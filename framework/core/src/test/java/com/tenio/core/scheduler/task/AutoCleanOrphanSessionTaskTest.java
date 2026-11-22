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

package com.tenio.core.scheduler.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For AutoCleanOrphanSessionTask")
class AutoCleanOrphanSessionTaskTest {

  private SessionManager sessionManager;
  private AutoCleanOrphanSessionTask task;

  @BeforeEach
  void setUp() {
    sessionManager = Mockito.mock(SessionManager.class);
    task = new AutoCleanOrphanSessionTask();
    task.setSessionManager(sessionManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(new AutoCleanOrphanSessionTask());
  }

  @Test
  @DisplayName("Test session manager setter")
  void testSetSessionManager() {
    task.setSessionManager(sessionManager);
    // No exception means success
  }

  @Test
  @DisplayName("lambda body: orphan session is closed and removed")
  void testLambdaBodyClosesAndRemovesOrphanSession() throws IOException {
    Session orphan = Mockito.mock(Session.class);
    Mockito.when(orphan.isOrphan()).thenReturn(true);
    Mockito.when(sessionManager.getSnapshotSessionsList()).thenReturn(List.of(orphan));
    Mockito.when(sessionManager.getSnapshotSessionCount()).thenReturn(1);

    task.scan();

    Mockito.verify(orphan).close(ConnectionDisconnectMode.ORPHAN, PlayerDisconnectMode.CONNECTION_LOST);
    Mockito.verify(orphan).remove();
  }

  @Test
  @DisplayName("lambda body: non-orphan session is not closed")
  void testLambdaBodySkipsNonOrphanSession() throws IOException {
    Session session = Mockito.mock(Session.class);
    Mockito.when(session.isOrphan()).thenReturn(false);
    Mockito.when(sessionManager.getSnapshotSessionsList()).thenReturn(List.of(session));
    Mockito.when(sessionManager.getSnapshotSessionCount()).thenReturn(1);

    task.scan();

    Mockito.verify(session, Mockito.never()).close(Mockito.any(), Mockito.any());
  }

  @Test
  @DisplayName("lambda body: IOException during session.close() is caught without propagation")
  void testLambdaBodyHandlesIOExceptionDuringClose() throws IOException {
    Session orphan = Mockito.mock(Session.class);
    Mockito.when(orphan.isOrphan()).thenReturn(true);
    Mockito.doThrow(IOException.class).when(orphan).close(Mockito.any(), Mockito.any());
    Mockito.when(sessionManager.getSnapshotSessionsList()).thenReturn(List.of(orphan));
    Mockito.when(sessionManager.getSnapshotSessionCount()).thenReturn(1);

    assertDoesNotThrow(() -> task.scan());
    Mockito.verify(orphan).remove();
  }
}
