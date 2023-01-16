package org.infpls.royale.server.game.dao.user;

import java.io.IOException;
import java.util.*;
import org.springframework.web.socket.WebSocketSession;

import org.infpls.royale.server.game.dao.DaoContainer;
import org.infpls.royale.server.game.session.RoyaleSession;
import org.infpls.royale.server.game.session.RoyaleAccount;
import org.infpls.royale.server.util.Oak;

/* UserDao handles both user info and logged in user RoyaleSessions.
   This is because theres is an overlap in data here
   and seperating these things seems counter-intuitive.
*/

public class UserDao {
  private final List<RoyaleSession> sessions; /* This is a list of all active user RoyaleSessions. */
  private final List<RoyaleAccount> accounts; /* List of all accounts present in the Mario Royale Deluxe database */
  private final List<RoyaleAccount> loggedIn; /* Accounts players have logged into */
  
  public UserDao() {
    sessions = Collections.synchronizedList(new ArrayList());
    /* insert mongodb connecting shit and initializing the accounts */
    accounts = Collections.synchronizedList(new ArrayList());
    loggedIn = Collections.synchronizedList(new ArrayList());

    accounts.add(new RoyaleAccount("d", "d", "terminalarch", "terminalling", "lol", 12, 65535, 14, 2)); /* Testing account */
  }
  
  public RoyaleSession createSession(final WebSocketSession webSocket, DaoContainer dao) throws IOException {
    RoyaleSession session = new RoyaleSession(webSocket, dao);
    sessions.add(session);
    return session;
  }
  
  public void destroySession(final WebSocketSession webSocket) throws IOException {
    for(int i=0;i<sessions.size();i++) {
      if(sessions.get(i).getWebSocketId().equals(webSocket.getId())) {
        final RoyaleSession session = sessions.get(i);
        session.destroy();
        try { sessions.remove(session); }
        catch(Exception ex) { Oak.log(Oak.Level.ERR, "Error while destroying session.", ex); } /* @TODO: errors involve with this method */
        return;
      }
    }
  }
  
  public RoyaleSession getSessionByUser(final String user) {
    for(int i=0;i<sessions.size();i++) {
      if(sessions.get(i).loggedIn()) {
        if(sessions.get(i).getUser().equals(user)) {
          return sessions.get(i);
        }
      }
    }
    return null;
  }

  public List<String> getOnlineUserList() {
    final List<String> users = new ArrayList();
    for(int i=0;i<sessions.size();i++) {
      final RoyaleSession session = sessions.get(i);
      users.add(session.loggedIn() ? session.getUser() : "Session##"+session.getSessionId());
    }
    return users;
  }
  
  public int getOnlineUserCount(int mode) {
    int sum = 0;
    for(int i=0;i<sessions.size();i++) {
      final RoyaleSession session = sessions.get(i);
      if (session.gameMode == mode) {
        sum += 1;
      }
    }

    return sum;
  }
}
