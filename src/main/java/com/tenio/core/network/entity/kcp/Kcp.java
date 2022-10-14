//=====================================================================
//
// KCP - A Better ARQ Protocol Implementation
// skywind3000 (at) gmail.com, 2010-2011
//  
// Features:
// + Average RTT reduce 30% - 40% vs traditional ARQ like tcp.
// + Maximum RTT reduce three times vs tcp.
// + Lightweight, distributed as a single source file.
//
//=====================================================================

package com.tenio.core.network.entity.kcp;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class Kcp {

  private static final int DEFAULT_BYTE_BUFFER_SIZE = 10240;

  //=====================================================================
  // KCP BASIC
  //=====================================================================
  private static final int IKCP_RTO_NDL = 30;   // no delay min rto
  private static final int IKCP_RTO_MIN = 100;  // normal min rto
  private static final int IKCP_RTO_DEF = 200;
  private static final int IKCP_RTO_MAX = 60000;
  private static final int IKCP_CMD_PUSH = 81;  // cmd: push data
  private static final int IKCP_CMD_ACK = 82;   // cmd: ack
  private static final int IKCP_CMD_WASK = 83;  // cmd: window probe (ask)
  private static final int IKCP_CMD_WINS = 84;  // cmd: window size (tell)
  private static final int IKCP_ASK_SEND = 1;   // need to send IKCP_CMD_WASK
  private static final int IKCP_ASK_TELL = 2;   // need to send IKCP_CMD_WINS
  private static final int IKCP_WND_SND = 32;
  private static final int IKCP_WND_RCV = 32;
  private static final int IKCP_MTU_DEF = 1400;
  private static final int IKCP_ACK_FAST = 3;
  private static final int IKCP_INTERVAL = 100;
  private static final int IKCP_OVERHEAD = 24;
  private static final int IKCP_DEADLINK = 10;
  private static final int IKCP_THRESH_INIT = 2;
  private static final int IKCP_THRESH_MIN = 2;
  private static final int IKCP_PROBE_INIT = 7000;    // 7 secs to probe window size
  private static final int IKCP_PROBE_LIMIT = 120000; // up to 120 secs to probe window

  private final long conv;
  private final ArrayList<Segment> nrcv_buf = new ArrayList<>(128);
  private final ArrayList<Segment> nsnd_buf = new ArrayList<>(128);
  private final ArrayList<Segment> nrcv_que = new ArrayList<>(128);
  private final ArrayList<Segment> nsnd_que = new ArrayList<>(128);
  private final ArrayList<Long> acklist = new ArrayList<>(128);
  private final long dead_link = IKCP_DEADLINK;
  private long snd_una = 0;
  private long snd_nxt = 0;
  private long rcv_nxt = 0;
  private long ts_probe = 0;
  private long probe_wait = 0;
  private long snd_wnd = IKCP_WND_SND;
  private long rcv_wnd = IKCP_WND_RCV;
  private long rmt_wnd = IKCP_WND_RCV;
  private long cwnd = 0;
  private long incr = 0;
  private long probe = 0;
  private long mtu = IKCP_MTU_DEF;
  private long mss = this.mtu - IKCP_OVERHEAD;
  private byte[] buffer = new byte[(int) (mtu + IKCP_OVERHEAD) * 3];
  private long state = 0;
  private long rx_srtt = 0;
  private long rx_rttval = 0;
  private long rx_rto = IKCP_RTO_DEF;
  private long rx_minrto = IKCP_RTO_MIN;
  private long current = 0;
  private long interval = IKCP_INTERVAL;
  private long ts_flush = IKCP_INTERVAL;
  private long nodelay = 0;
  private long updated = 0;
  private long ssthresh = IKCP_THRESH_INIT;
  private long fastresend = 0;
  private long nocwnd = 0;
  private long xmit = 0;

  public Kcp(long conv) {
    this.conv = conv;
  }

  private int itimediff(long later, long earlier) {
    return ((int) (later - earlier));
  }

  // encode 8 bits unsigned int
  private void encode8u(byte[] p, int offset, byte c) {
    p[0 + offset] = c;
  }

  // decode 8 bits unsigned int
  private byte decode8u(byte[] p, int offset) {
    return p[0 + offset];
  }

  /* encode 16 bits unsigned int (msb) */
  private void encode16u(byte[] p, int offset, int w) {
    p[offset + 0] = (byte) (w >> 8);
    p[offset + 1] = (byte) (w >> 0);
  }

  /* decode 16 bits unsigned int (msb) */
  private int decode16u(byte[] p, int offset) {
    int ret = (p[offset + 0] & 0xFF) << 8
        | (p[offset + 1] & 0xFF);
    return ret;
  }

  /* encode 32 bits unsigned int (msb) */
  private void encode32u(byte[] p, int offset, long l) {
    p[offset + 0] = (byte) (l >> 24);
    p[offset + 1] = (byte) (l >> 16);
    p[offset + 2] = (byte) (l >> 8);
    p[offset + 3] = (byte) (l >> 0);
  }

  /* decode 32 bits unsigned int (msb) */
  private long decode32u(byte[] p, int offset) {
    long ret = (p[offset + 0] & 0xFFL) << 24
        | (p[offset + 1] & 0xFFL) << 16
        | (p[offset + 2] & 0xFFL) << 8
        | p[offset + 3] & 0xFFL;
    return ret;
  }

  private void slice(ArrayList list, int start, int stop) {
    int size = list.size();
    for (int i = 0; i < size; ++i) {
      if (i < stop - start) {
        list.set(i, list.get(i + start));
      } else {
        list.remove(stop - start);
      }
    }
  }

  private long min(long a, long b) {
    return a <= b ? a : b;
  }

  private long max(long a, long b) {
    return a >= b ? a : b;
  }

  private long bound(long lower, long middle, long upper) {
    return min(max(lower, middle), upper);
  }

  protected abstract void Output(byte[] buffer, int size);

  //---------------------------------------------------------------------
  // user/upper level recv: returns size, returns below zero for EAGAIN
  //---------------------------------------------------------------------
  public int Recv(Consumer<byte[]> receiving) {

    if (0 == nrcv_que.size()) {
      return -1;
    }

    int peekSize = peekSize();
    if (0 > peekSize) {
      return -2;
    }

    byte[] buffer = new byte[DEFAULT_BYTE_BUFFER_SIZE];

    if (peekSize > buffer.length) {
      return -3;
    }

    boolean recover = nrcv_que.size() >= rcv_wnd;

    // merge fragment.
    int count = 0;
    int n = 0;
    for (Segment seg : nrcv_que) {
      System.arraycopy(seg.data, 0, buffer, n, seg.data.length);
      n += seg.data.length;
      count++;
      if (0 == seg.frg) {
        break;
      }
    }

    if (0 < count) {
      slice(nrcv_que, count, nrcv_que.size());
    }

    // move available data from rcv_buf -> nrcv_que
    count = 0;
    for (Segment seg : nrcv_buf) {
      if (seg.sn == rcv_nxt && nrcv_que.size() < rcv_wnd) {
        nrcv_que.add(seg);
        rcv_nxt++;
        count++;
      } else {
        break;
      }
    }

    if (0 < count) {
      slice(nrcv_buf, count, nrcv_buf.size());
    }

    // fast recover
    if (nrcv_que.size() < rcv_wnd && recover) {
      // ready to send back IKCP_CMD_WINS in ikcp_flush
      // tell remote my window size
      probe |= IKCP_ASK_TELL;
    }

    receiving.accept(buffer);

    return n;
  }

  //---------------------------------------------------------------------
  // peek data size
  //---------------------------------------------------------------------
  // check the size of next message in the recv queue
  private int peekSize() {
    if (0 == nrcv_que.size()) {
      return -1;
    }

    Segment seq = nrcv_que.get(0);

    if (0 == seq.frg) {
      return seq.data.length;
    }

    if (nrcv_que.size() < seq.frg + 1) {
      return -1;
    }

    int length = 0;

    for (Segment item : nrcv_que) {
      length += item.data.length;
      if (0 == item.frg) {
        break;
      }
    }

    return length;
  }

  //---------------------------------------------------------------------
  // user/upper level send, returns below zero for error
  //---------------------------------------------------------------------
  public int Send(byte[] buffer) {

    if (0 == buffer.length) {
      return -1;
    }

    int count;

    // 根据mss大小分片
    if (buffer.length < mss) {
      count = 1;
    } else {
      count = (int) (buffer.length + mss - 1) / (int) mss;
    }

    if (255 < count) {
      return -2;
    }

    if (0 == count) {
      count = 1;
    }

    int offset = 0;

    // 分片后加入到发送队列
    int length = buffer.length;
    for (int i = 0; i < count; i++) {
      int size = (int) (length > mss ? mss : length);
      Segment seg = new Segment(size);
      System.arraycopy(buffer, offset, seg.data, 0, size);
      offset += size;
      seg.frg = count - i - 1;
      nsnd_que.add(seg);
      length -= size;
    }
    return 0;
  }

  //---------------------------------------------------------------------
  // parse ack
  //---------------------------------------------------------------------
  private void update_ack(int rtt) {
    if (0 == rx_srtt) {
      rx_srtt = rtt;
      rx_rttval = rtt / 2;
    } else {
      int delta = (int) (rtt - rx_srtt);
      if (0 > delta) {
        delta = -delta;
      }

      rx_rttval = (3 * rx_rttval + delta) / 4;
      rx_srtt = (7 * rx_srtt + rtt) / 8;
      if (rx_srtt < 1) {
        rx_srtt = 1;
      }
    }

    int rto = (int) (rx_srtt + max(1, 4 * rx_rttval));
    rx_rto = bound(rx_minrto, rto, IKCP_RTO_MAX);
  }

  // 计算本地真实snd_una
  void shrink_buf() {
    if (nsnd_buf.size() > 0) {
      snd_una = nsnd_buf.get(0).sn;
    } else {
      snd_una = snd_nxt;
    }
  }

  // 对端返回的ack, 确认发送成功时，对应包从发送缓存中移除
  private void parse_ack(long sn) {
    if (itimediff(sn, snd_una) < 0 || itimediff(sn, snd_nxt) >= 0) {
      return;
    }

    int index = 0;
    for (Segment seg : nsnd_buf) {
      if (itimediff(sn, seg.sn) < 0) {
        break;
      }

      // 原版ikcp_parse_fastack&ikcp_parse_ack逻辑重复
      seg.fastack++;

      if (sn == seg.sn) {
        nsnd_buf.remove(index);
        break;
      }
      index++;
    }
  }

  // 通过对端传回的una将已经确认发送成功包从发送缓存中移除
  private void parse_una(long una) {
    int count = 0;
    for (Segment seg : nsnd_buf) {
      if (itimediff(una, seg.sn) > 0) {
        count++;
      } else {
        break;
      }
    }

    if (0 < count) {
      slice(nsnd_buf, count, nsnd_buf.size());
    }
  }

  //---------------------------------------------------------------------
  // ack append
  //---------------------------------------------------------------------
  // 收数据包后需要给对端回ack，flush时发送出去
  private void ack_push(long sn, long ts) {
    // c原版实现中按*2扩大容量
    acklist.add(sn);
    acklist.add(ts);
  }

  //---------------------------------------------------------------------
  // parse data
  //---------------------------------------------------------------------
  // 用户数据包解析
  private void parse_data(Segment newseg) {
    long sn = newseg.sn;
    boolean repeat = false;

    if (itimediff(sn, rcv_nxt + rcv_wnd) >= 0 || itimediff(sn, rcv_nxt) < 0) {
      return;
    }

    int n = nrcv_buf.size() - 1;
    int after_idx = -1;

    // 判断是否是重复包，并且计算插入位置
    for (int i = n; i >= 0; i--) {
      Segment seg = nrcv_buf.get(i);
      if (seg.sn == sn) {
        repeat = true;
        break;
      }

      if (itimediff(sn, seg.sn) > 0) {
        after_idx = i;
        break;
      }
    }

    // 如果不是重复包，则插入
    if (!repeat) {
      if (after_idx == -1) {
        nrcv_buf.add(0, newseg);
      } else {
        nrcv_buf.add(after_idx + 1, newseg);
      }
    }

    // move available data from nrcv_buf -> nrcv_que
    // 将连续包加入到接收队列
    int count = 0;
    for (Segment seg : nrcv_buf) {
      if (seg.sn == rcv_nxt && nrcv_que.size() < rcv_wnd) {
        nrcv_que.add(seg);
        rcv_nxt++;
        count++;
      } else {
        break;
      }
    }

    // 从接收缓存中移除
    if (0 < count) {
      slice(nrcv_buf, count, nrcv_buf.size());
    }
  }

  // when you received a low level packet (eg. UDP packet), call it
  //---------------------------------------------------------------------
  // input data
  //---------------------------------------------------------------------
  public int Input(byte[] data) {

    long s_una = snd_una;
    if (data.length < IKCP_OVERHEAD) {
      return 0;
    }

    int offset = 0;

    while (true) {
      long ts, sn, length, una, conv_;
      int wnd;
      byte cmd, frg;

      if (data.length - offset < IKCP_OVERHEAD) {
        break;
      }

      conv_ = decode32u(data, offset);
      offset += 4;
      if (conv != conv_) {
        return -1;
      }

      cmd = decode8u(data, offset);
      offset += 1;
      frg = decode8u(data, offset);
      offset += 1;
      wnd = decode16u(data, offset);
      offset += 2;
      ts = decode32u(data, offset);
      offset += 4;
      sn = decode32u(data, offset);
      offset += 4;
      una = decode32u(data, offset);
      offset += 4;
      length = decode32u(data, offset);
      offset += 4;

      if (data.length - offset < length) {
        return -2;
      }

      if (cmd != IKCP_CMD_PUSH && cmd != IKCP_CMD_ACK && cmd != IKCP_CMD_WASK &&
          cmd != IKCP_CMD_WINS) {
        return -3;
      }

      rmt_wnd = wnd;
      parse_una(una);
      shrink_buf();

      if (IKCP_CMD_ACK == cmd) {
        if (itimediff(current, ts) >= 0) {
          update_ack(itimediff(current, ts));
        }
        parse_ack(sn);
        shrink_buf();
      } else if (IKCP_CMD_PUSH == cmd) {
        if (itimediff(sn, rcv_nxt + rcv_wnd) < 0) {
          ack_push(sn, ts);
          if (itimediff(sn, rcv_nxt) >= 0) {
            Segment seg = new Segment((int) length);
            seg.conv = conv_;
            seg.cmd = cmd;
            seg.frg = frg;
            seg.wnd = wnd;
            seg.ts = ts;
            seg.sn = sn;
            seg.una = una;

            if (length > 0) {
              System.arraycopy(data, offset, seg.data, 0, (int) length);
            }

            parse_data(seg);
          }
        }
      } else if (IKCP_CMD_WASK == cmd) {
        // ready to send back IKCP_CMD_WINS in Ikcp_flush
        // tell remote my window size
        probe |= IKCP_ASK_TELL;
      } else if (IKCP_CMD_WINS == cmd) {
        // do nothing
      } else {
        return -3;
      }

      offset += (int) length;
    }

    if (itimediff(snd_una, s_una) > 0) {
      if (cwnd < rmt_wnd) {
        long mss_ = mss;
        if (cwnd < ssthresh) {
          cwnd++;
          incr += mss_;
        } else {
          if (incr < mss_) {
            incr = mss_;
          }
          incr += (mss_ * mss_) / incr + (mss_ / 16);
          if ((cwnd + 1) * mss_ <= incr) {
            cwnd++;
          }
        }
        if (cwnd > rmt_wnd) {
          cwnd = rmt_wnd;
          incr = rmt_wnd * mss_;
        }
      }
    }

    return 0;
  }

  // 接收窗口可用大小
  private int wnd_unused() {
    if (nrcv_que.size() < rcv_wnd) {
      return (int) rcv_wnd - nrcv_que.size();
    }
    return 0;
  }

  //---------------------------------------------------------------------
  // ikcp_flush
  //---------------------------------------------------------------------
  private void flush() {
    long current_ = current;
    byte[] buffer_ = buffer;
    int change = 0;
    int lost = 0;

    // 'ikcp_update' haven't been called.
    if (0 == updated) {
      return;
    }

    Segment seg = new Segment(0);
    seg.conv = conv;
    seg.cmd = IKCP_CMD_ACK;
    seg.wnd = wnd_unused();
    seg.una = rcv_nxt;

    // flush acknowledges
    // 将acklist中的ack发送出去
    int count = acklist.size() / 2;
    int offset = 0;
    for (int i = 0; i < count; i++) {
      if (offset + IKCP_OVERHEAD > mtu) {
        Output(buffer, offset);
        offset = 0;
      }
      // ikcp_ack_get
      seg.sn = acklist.get(i * 2 + 0);
      seg.ts = acklist.get(i * 2 + 1);
      offset += seg.encode(buffer, offset);
    }
    acklist.clear();

    // probe window size (if remote window size equals zero)
    // rmt_wnd=0时，判断是否需要请求对端接收窗口
    if (0 == rmt_wnd) {
      if (0 == probe_wait) {
        probe_wait = IKCP_PROBE_INIT;
        ts_probe = current + probe_wait;
      } else {
        // 逐步扩大请求时间间隔
        if (itimediff(current, ts_probe) >= 0) {
          if (probe_wait < IKCP_PROBE_INIT) {
            probe_wait = IKCP_PROBE_INIT;
          }
          probe_wait += probe_wait / 2;
          if (probe_wait > IKCP_PROBE_LIMIT) {
            probe_wait = IKCP_PROBE_LIMIT;
          }
          ts_probe = current + probe_wait;
          probe |= IKCP_ASK_SEND;
        }
      }
    } else {
      ts_probe = 0;
      probe_wait = 0;
    }

    // flush window probing commands
    // 请求对端接收窗口
    if ((probe & IKCP_ASK_SEND) != 0) {
      seg.cmd = IKCP_CMD_WASK;
      if (offset + IKCP_OVERHEAD > mtu) {
        Output(buffer, offset);
        offset = 0;
      }
      offset += seg.encode(buffer, offset);
    }

    // flush window probing commands(c#)
    // 告诉对端自己的接收窗口
    if ((probe & IKCP_ASK_TELL) != 0) {
      seg.cmd = IKCP_CMD_WINS;
      if (offset + IKCP_OVERHEAD > mtu) {
        Output(buffer, offset);
        offset = 0;
      }
      offset += seg.encode(buffer, offset);
    }

    probe = 0;

    // calculate window size
    long cwnd_ = min(snd_wnd, rmt_wnd);
    // 如果采用拥塞控制
    if (0 == nocwnd) {
      cwnd_ = min(cwnd, cwnd_);
    }

    count = 0;
    // move data from snd_queue to snd_buf
    for (Segment nsnd_que1 : nsnd_que) {
      if (itimediff(snd_nxt, snd_una + cwnd_) >= 0) {
        break;
      }
      Segment newseg = nsnd_que1;
      newseg.conv = conv;
      newseg.cmd = IKCP_CMD_PUSH;
      newseg.wnd = seg.wnd;
      newseg.ts = current_;
      newseg.sn = snd_nxt;
      newseg.una = rcv_nxt;
      newseg.resendts = current_;
      newseg.rto = rx_rto;
      newseg.fastack = 0;
      newseg.xmit = 0;
      nsnd_buf.add(newseg);
      snd_nxt++;
      count++;
    }

    if (0 < count) {
      slice(nsnd_que, count, nsnd_que.size());
    }

    // calculate resent
    long resent = (fastresend > 0) ? fastresend : 0xffffffff;
    long rtomin = (nodelay == 0) ? (rx_rto >> 3) : 0;

    // flush data segments
    for (Segment segment : nsnd_buf) {
      boolean needsend = false;
      if (0 == segment.xmit) {
        // 第一次传输
        needsend = true;
        segment.xmit++;
        segment.rto = rx_rto;
        segment.resendts = current_ + segment.rto + rtomin;
      } else if (itimediff(current_, segment.resendts) >= 0) {
        // 丢包重传
        needsend = true;
        segment.xmit++;
        xmit++;
        if (0 == nodelay) {
          segment.rto += rx_rto;
        } else {
          segment.rto += rx_rto / 2;
        }
        segment.resendts = current_ + segment.rto;
        lost = 1;
      } else if (segment.fastack >= resent) {
        // 快速重传
        needsend = true;
        segment.xmit++;
        segment.fastack = 0;
        segment.resendts = current_ + segment.rto;
        change++;
      }

      if (needsend) {
        segment.ts = current_;
        segment.wnd = seg.wnd;
        segment.una = rcv_nxt;

        int need = IKCP_OVERHEAD + segment.data.length;
        if (offset + need >= mtu) {
          Output(buffer, offset);
          offset = 0;
        }

        offset += segment.encode(buffer, offset);
        if (segment.data.length > 0) {
          System.arraycopy(segment.data, 0, buffer, offset, segment.data.length);
          offset += segment.data.length;
        }

        if (segment.xmit >= dead_link) {
          state = -1; // state = 0(c#)
        }
      }
    }

    // flash remain segments
    if (offset > 0) {
      Output(buffer, offset);
    }

    // update ssthresh
    // 拥塞避免
    if (change != 0) {
      long inflight = snd_nxt - snd_una;
      ssthresh = inflight / 2;
      if (ssthresh < IKCP_THRESH_MIN) {
        ssthresh = IKCP_THRESH_MIN;
      }
      cwnd = ssthresh + resent;
      incr = cwnd * mss;
    }

    if (lost != 0) {
      ssthresh = cwnd / 2;
      if (ssthresh < IKCP_THRESH_MIN) {
        ssthresh = IKCP_THRESH_MIN;
      }
      cwnd = 1;
      incr = mss;
    }

    if (cwnd < 1) {
      cwnd = 1;
      incr = mss;
    }
  }

  //---------------------------------------------------------------------
  // update state (call it repeatedly, every 10ms-100ms), or you can ask
  // ikcp_check when to call it again (without ikcp_input/_send calling).
  // 'current' - current timestamp in millisec.
  //---------------------------------------------------------------------
  public void Update(long current) {
    this.current = current;

    // 首次调用Update
    if (0 == updated) {
      updated = 1;
      ts_flush = this.current;
    }

    // 两次更新间隔
    int slap = itimediff(this.current, ts_flush);

    // interval设置过大或者Update调用间隔太久
    if (slap >= 10000 || slap < -10000) {
      ts_flush = this.current;
      slap = 0;
    }

    // flush同时设置下一次更新时间
    if (slap >= 0) {
      ts_flush += interval;
      if (itimediff(this.current, ts_flush) >= 0) {
        ts_flush = this.current + interval;
      }
      flush();
    }
  }

  //---------------------------------------------------------------------
  // Determine when should you invoke ikcp_update:
  // returns when you should invoke ikcp_update in millisec, if there
  // is no ikcp_input/_send calling. you can call ikcp_update in that
  // time, instead of call update repeatly.
  // Important to reduce unnacessary ikcp_update invoking. use it to
  // schedule ikcp_update (eg. implementing an epoll-like mechanism,
  // or optimize ikcp_update when handling massive kcp connections)
  //---------------------------------------------------------------------
  public long Check(long current) {

    long ts_flush_ = ts_flush;
    long tm_flush = 0x7fffffff;
    long tm_packet = 0x7fffffff;
    long minimal;

    if (0 == updated) {
      return current;
    }

    if (itimediff(current, ts_flush_) >= 10000 || itimediff(current, ts_flush_) < -10000) {
      ts_flush_ = current;
    }

    if (itimediff(current, ts_flush_) >= 0) {
      return current;
    }

    tm_flush = itimediff(ts_flush_, current);

    for (Segment seg : nsnd_buf) {
      int diff = itimediff(seg.resendts, current);
      if (diff <= 0) {
        return current;
      }
      if (diff < tm_packet) {
        tm_packet = diff;
      }
    }

    minimal = tm_packet < tm_flush ? tm_packet : tm_flush;
    if (minimal >= interval) {
      minimal = interval;
    }

    return current + minimal;
  }

  // change MTU size, default is 1400
  public int SetMtu(int mtu_) {
    if (mtu_ < 50 || mtu_ < IKCP_OVERHEAD) {
      return -1;
    }

    byte[] buffer = new byte[(mtu_ + IKCP_OVERHEAD) * 3];
    if (null == buffer) {
      return -2;
    }

    mtu = mtu_;
    mss = mtu - IKCP_OVERHEAD;
    this.buffer = buffer;
    return 0;
  }

  public int SetInterval(int interval) {
    if (interval > 5000) {
      interval = 5000;
    } else if (interval < 10) {
      interval = 10;
    }
    this.interval = interval;
    return 0;
  }

  // fastest: ikcp_nodelay(kcp, 1, 20, 2, 1)
  // nodelay: 0:disable(default), 1:enable
  // interval: internal update timer interval in millisec, default is 100ms
  // resend: 0:disable fast resend(default), 1:enable fast resend
  // nc: 0:normal congestion control(default), 1:disable congestion control
  public int SetNoDelay(int nodelay, int interval, int resend, int nc) {

    if (nodelay >= 0) {
      this.nodelay = nodelay;
      if (nodelay != 0) {
        rx_minrto = IKCP_RTO_NDL;
      } else {
        rx_minrto = IKCP_RTO_MIN;
      }
    }

    if (interval >= 0) {
      if (interval > 5000) {
        interval = 5000;
      } else if (interval < 10) {
        interval = 10;
      }
      this.interval = interval;
    }

    if (resend >= 0) {
      fastresend = resend;
    }

    if (nc >= 0) {
      nocwnd = nc;
    }

    return 0;
  }

  // set maximum window size: sndwnd=32, rcvwnd=32 by default
  public int SetWndSize(int sndwnd, int rcvwnd) {
    if (sndwnd > 0) {
      snd_wnd = sndwnd;
    }

    if (rcvwnd > 0) {
      rcv_wnd = rcvwnd;
    }
    return 0;
  }

  // get how many packet is waiting to be sent
  public int GetWaitSnd() {
    return nsnd_buf.size() + nsnd_que.size();
  }

  private class Segment {

    private long conv = 0;
    private long cmd = 0;
    private long frg = 0;
    private long wnd = 0;
    private long ts = 0;
    private long sn = 0;
    private long una = 0;
    private long resendts = 0;
    private long rto = 0;
    private long fastack = 0;
    private long xmit = 0;
    private final byte[] data;

    private Segment(int size) {
      this.data = new byte[size];
    }

    //---------------------------------------------------------------------
    // ikcp_encode_seg
    //---------------------------------------------------------------------
    // encode a segment into buffer
    private int encode(byte[] ptr, int offset) {
      int oldOffset = offset;

      encode32u(ptr, offset, conv);
      offset += 4;
      encode8u(ptr, offset, (byte) cmd);
      offset += 1;
      encode8u(ptr, offset, (byte) frg);
      offset += 1;
      encode16u(ptr, offset, (int) wnd);
      offset += 2;
      encode32u(ptr, offset, ts);
      offset += 4;
      encode32u(ptr, offset, sn);
      offset += 4;
      encode32u(ptr, offset, una);
      offset += 4;
      encode32u(ptr, offset, data.length);
      offset += 4;

      return offset - oldOffset;
    }
  }
}
