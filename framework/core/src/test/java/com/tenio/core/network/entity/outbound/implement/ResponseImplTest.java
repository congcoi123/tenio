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

package com.tenio.core.network.entity.outbound.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.core.entity.Player;
import com.tenio.core.network.define.ResponseGuarantee;
import com.tenio.core.network.entity.outbound.Response;
import com.tenio.core.network.entity.session.Session;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponseImplTest {

  private Response response;

  @BeforeEach
  void setUp() {
    response = ResponseImpl.newInstance();
  }

  @Test
  void testNewInstance() {
    ResponseImpl.newInstance();
  }

  @Test
  void testNewInstanceCreatesNonNull() {
    assertNotNull(response);
  }

  @Test
  void testDefaultGuaranteeIsNormal() {
    assertEquals(ResponseGuarantee.NORMAL, response.getGuarantee());
  }

  @Test
  void testDefaultNeedsEncryptedIsFalse() {
    assertFalse(response.needsEncrypted());
  }

  @Test
  void testDefaultRecipientCollectionsAreNull() {
    assertNull(response.getRecipientPlayers());
    assertNull(response.getRecipientSocketSessions());
    assertNull(response.getRecipientDatagramSessions());
    assertNull(response.getRecipientWebSocketSessions());
    assertNull(response.getNonSessionRecipientPlayers());
  }

  @Test
  void testSetAndGetContent() {
    DataCollection content = mock(DataCollection.class);
    response.setContent(content);
    assertEquals(content, response.getContent());
  }

  @Test
  void testGetDataTypeDelegatesToContent() {
    DataCollection content = mock(DataCollection.class);
    when(content.getType()).thenReturn(DataType.MSG_PACK);
    response.setContent(content);
    assertEquals(DataType.MSG_PACK, response.getDataType());
  }

  @Test
  void testGuaranteeSetterAndGetter() {
    response.guarantee(ResponseGuarantee.GUARANTEED);
    assertEquals(ResponseGuarantee.GUARANTEED, response.getGuarantee());
  }

  @Test
  void testEncryptedFlagIsSetByEncrypted() {
    response.encrypted();
    assertTrue(response.needsEncrypted());
  }

  @Test
  void testSetRecipientPlayerAddsSinglePlayer() {
    Player player = mock(Player.class);
    response.setRecipientPlayer(player);
    assertNotNull(response.getRecipientPlayers());
    assertTrue(response.getRecipientPlayers().contains(player));
  }

  @Test
  void testSetRecipientPlayersAddsCollection() {
    Player player1 = mock(Player.class);
    Player player2 = mock(Player.class);
    response.setRecipientPlayers(List.of(player1, player2));
    assertEquals(2, response.getRecipientPlayers().size());
  }

  @Test
  void testSetRecipientSessionTcpGoesToSocketSessions() {
    Session session = mock(Session.class);
    when(session.isTcp()).thenReturn(true);
    when(session.containsUdp()).thenReturn(false);

    response.setRecipientSession(session);

    assertNotNull(response.getRecipientSocketSessions());
    assertTrue(response.getRecipientSocketSessions().contains(session));
    assertNull(response.getRecipientDatagramSessions());
  }

  @Test
  void testSetRecipientSessionTcpWithUdpAndPrioritizedUdpGoesToDatagramSessions() {
    Session session = mock(Session.class);
    when(session.isTcp()).thenReturn(true);
    when(session.containsUdp()).thenReturn(true);

    response.prioritizedUdp().setRecipientSession(session);

    assertNotNull(response.getRecipientDatagramSessions());
    assertTrue(response.getRecipientDatagramSessions().contains(session));
    assertNull(response.getRecipientSocketSessions());
  }

  @Test
  void testSetRecipientSessionWebSocketGoesToWebSocketSessions() {
    Session session = mock(Session.class);
    when(session.isTcp()).thenReturn(false);
    when(session.isWebSocket()).thenReturn(true);

    response.setRecipientSession(session);

    assertNotNull(response.getRecipientWebSocketSessions());
    assertTrue(response.getRecipientWebSocketSessions().contains(session));
    assertNull(response.getRecipientSocketSessions());
  }

  @Test
  void testSetRecipientSessionsAddsMultiple() {
    Session s1 = mock(Session.class);
    Session s2 = mock(Session.class);
    when(s1.isTcp()).thenReturn(true);
    when(s1.containsUdp()).thenReturn(false);
    when(s2.isTcp()).thenReturn(true);
    when(s2.containsUdp()).thenReturn(false);

    response.setRecipientSessions(List.of(s1, s2));

    assertEquals(2, response.getRecipientSocketSessions().size());
  }

  @Test
  void testToStringIsNotNull() {
    assertNotNull(response.toString());
    assertTrue(response.toString().contains("Response"));
  }
}
