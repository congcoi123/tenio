/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.command.system.implement;

import com.google.common.net.InetAddresses;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.server.ServerImpl;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.utility.CommandUtility;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Removes an IP address from the active connection filter.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "unban", profile = CoreConstant.MONITOR_PROFILE,
    usage = {"<ip-address>"},
    description = "Removes an IP address from the ban list")
public final class UnBanAddressCommand extends AbstractSystemCommandHandler {

  private final Supplier<ConnectionFilter> filter;

  public UnBanAddressCommand() {
    this(() -> ServerImpl.getInstance().getConnectionFilter());
  }

  UnBanAddressCommand(Supplier<ConnectionFilter> filter) {
    this.filter = filter;
  }

  @Override
  public void execute(List<String> arguments) {
    if (arguments.size() != 1 || !InetAddresses.isInetAddress(arguments.getFirst())) {
      CommandUtility.INSTANCE.showConsoleMessage("Usage: unban <ip-address>");
      return;
    }
    var activeFilter = filter.get();
    if (activeFilter == null) {
      CommandUtility.INSTANCE.showConsoleMessage("The connection filter is unavailable.");
      return;
    }
    String input = arguments.getFirst();
    String canonical = InetAddresses.forString(input).getHostAddress();
    var banned = Arrays.asList(activeFilter.getBannedAddresses());
    if (banned.contains(input)) {
      activeFilter.removeBannedAddress(input);
    } else if (banned.contains(canonical)) {
      activeFilter.removeBannedAddress(canonical);
    } else {
      CommandUtility.INSTANCE.showConsoleMessage("Address is not banned: " + input);
      return;
    }
    CommandUtility.INSTANCE.showConsoleMessage("Unbanned " + input);
  }
}
