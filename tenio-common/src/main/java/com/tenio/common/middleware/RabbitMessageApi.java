package com.tenio.common.middleware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tengames.basegame.message.ChannelBaseGameApi;
import com.tengames.basegame.message.CommandBaseGameApi;
import com.tengames.basegame.message.ReqBaseGameApi;
import com.tengames.basegame.message.ResBaseGameApi;
import com.tengames.goldminer.db.DBContext;
import com.tengames.goldminer.db.dao.CcuDAO;
import com.tengames.goldminer.db.dao.PlayerDAO;
import com.tengames.goldminer.db.model.PlayerBean;
import com.tengames.goldminer.handlers.GameLogic;
import com.tenio.entities.manager.api.PlayerApi;
import com.tenio.entities.manager.api.RoomApi;
import com.tenio.exception.InvalidRequestParametersException;
import com.tenio.exception.TException;
import com.tenio.logger.AbstractLogger;

/**
 * 
 * @Author
 * kong
 * @Time
 * Jun 16, 2018
 * @Todo
 */
public class RabbitMessageApi extends AbstractLogger {

	private GameLogic game = GameLogic.getInstance();
	private PlayerApi playerApi = PlayerApi.getInstance();
	private RoomApi roomApi = RoomApi.getInstance();
	private CcuDAO ccuDao = (CcuDAO) DBContext.getInstance().getBean("ccuDAO");
	private PlayerDAO playerDao = (PlayerDAO) DBContext.getInstance().getBean("playerDAO");
	
	// GET
	public Map<String, Object> get(Map<String, Object> request) {
		try {
			if (request.containsKey(ReqBaseGameApi.CHANNEL)) {
				switch ((int) request.get(ReqBaseGameApi.CHANNEL)) {
				case ChannelBaseGameApi.CCU:
					return ccu(request);
				case ChannelBaseGameApi.FETCH:
					return fetch(request);
				case ChannelBaseGameApi.LOGIC:
					return logic(request);
				}
			} else
				throw TException.INVALID_REQUEST_PARAMETERS;
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return new HashMap<String, Object>();
	}
	
	// CCU
	private Map<String, Object> ccu(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.FROM_TIME) && request.containsKey(ReqBaseGameApi.TO_TIME)) {
				long fromTime = Long.parseLong(String.valueOf(request.get(ReqBaseGameApi.FROM_TIME)));
				long toTime = Long.parseLong(String.valueOf(request.get(ReqBaseGameApi.TO_TIME)));
				List<Map<String, Integer>> data = ccuDao.filter(fromTime, toTime);
				if (data.isEmpty())
					return response;
				response.put(ResBaseGameApi.DATA, data);
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	// Fetch
	private Map<String, Object> fetch(Map<String, Object> request) {
		try {
			if (request.containsKey(ReqBaseGameApi.COMMAND)) {
				int type = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.COMMAND)));
				switch (type) {
				case CommandBaseGameApi.FETCH_PLAYER:
					return fetchPlayer(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_NAME:
					return filterPlayerByName(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_TOP:
					return filterPlayerByTop(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_REGISTED_TIME:
					return filterPlayerByRegistedTime(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_LOGIN_TIME:
					return filterPlayerByLoginTime(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_LOGOUT_TIME:
					return filterPlayerByLogoutTime(request);
				case CommandBaseGameApi.FILTER_PLAYER_BY_CURRENT:
					return filterPlayerByCurrent(request);
				case CommandBaseGameApi.FILTER_ROOM_BY_NAME:
					return filterRoomByName(request);
				case CommandBaseGameApi.FETCH_TOP_PLAYER:
					return fetchTopPlayer(request);
				}
			} else
				throw TException.INVALID_REQUEST_PARAMETERS;
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return new HashMap<String, Object>();
	}
	
	private Map<String, Object> fetchPlayer(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.USER_NAME)) {
				String userName = String.valueOf(request.get(ReqBaseGameApi.USER_NAME));
				PlayerBean bean = playerDao.get(userName);
				if (bean == null)
					return response;
				List<Object> object = new ArrayList<Object>();
				object.add(bean.getWin());
				object.add(bean.getDraw());
				object.add(bean.getLose());
				object.add(bean.getLoginTime());
				object.add(bean.getLogoutTime());
				response.put(ResBaseGameApi.DATA, object);
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByName(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				response.put(ResBaseGameApi.COUNT, playerDao.count());
				response.put(ResBaseGameApi.DATA, playerDao.filterByName(numberElement, page, asc));
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByTop(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				response.put(ResBaseGameApi.COUNT, playerDao.count());
				response.put(ResBaseGameApi.DATA, playerDao.filterByTop(numberElement, page, asc));
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByRegistedTime(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				response.put(ResBaseGameApi.COUNT, playerDao.count());
				response.put(ResBaseGameApi.DATA, playerDao.filterByRegistedTime(numberElement, page, asc));
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByLoginTime(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				response.put(ResBaseGameApi.COUNT, playerDao.count());
				response.put(ResBaseGameApi.DATA, playerDao.filterByLoginTime(numberElement, page, asc));
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByLogoutTime(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				response.put(ResBaseGameApi.COUNT, playerDao.count());
				response.put(ResBaseGameApi.DATA, playerDao.filterByLogoutTime(numberElement, page, asc));
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> filterPlayerByCurrent(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				List<List<Object>> list = playerApi.getAllPlayerBaseInfos();
				
				if (!asc)
					Collections.reverse(list);
				
				int size = list.size();
				response.put(ResBaseGameApi.COUNT, size);
				
				int fromIndex = page * numberElement;
				int toIndex = (page + 1) * numberElement;
				if (fromIndex >= size)
					response.put(ResBaseGameApi.DATA, list);
				else {
					if (fromIndex >= toIndex)
						response.put(ResBaseGameApi.DATA, list);
					else {
						if (toIndex > size)
							toIndex = size;
						response.put(ResBaseGameApi.DATA, list.subList(fromIndex, toIndex));
					}
				}				
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> fetchTopPlayer(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put(ResBaseGameApi.DATA, playerDao.sortByTop(true));
		return response;
	}
	
	private Map<String, Object> filterRoomByName(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.NUMBER_ELEMENT)
					&& request.containsKey(ReqBaseGameApi.PAGE)
					&& request.containsKey(ReqBaseGameApi.SORT_ASC)) {
				int numberElement = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.NUMBER_ELEMENT)));
				int page = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.PAGE)));
				boolean asc = Boolean.parseBoolean(String.valueOf(request.get(ReqBaseGameApi.SORT_ASC)));
				
				List<List<Object>> list = roomApi.getAllRoomInfos();
				
				if (!asc)
					Collections.reverse(list);
				
				int size = list.size();
				response.put(ResBaseGameApi.COUNT, size);
				
				int fromIndex = page * numberElement;
				int toIndex = (page + 1) * numberElement;
				if (fromIndex >= size)
					response.put(ResBaseGameApi.DATA, list);
				else {
					if (fromIndex >= toIndex)
						response.put(ResBaseGameApi.DATA, list);
					else {
						if (toIndex > size)
							toIndex = size;
						response.put(ResBaseGameApi.DATA, list.subList(fromIndex, toIndex));
					}
				}				
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	// LOGIC
	private Map<String, Object> logic(Map<String, Object> request) {
		try {
			if (request.containsKey(ReqBaseGameApi.COMMAND)) {
				int type = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.COMMAND)));
				switch (type) {
				case CommandBaseGameApi.PUSH_BROAD_CAST:
					return pushBroadCast(request);
				case CommandBaseGameApi.MANUAL_PURCHASE:
					return manualPurchase(request);
				case CommandBaseGameApi.DELETE_PLAYER:
					return deletePlayer(request);
				case CommandBaseGameApi.KICK_PLAYER:
					return kickPlayer(request);
				}
			} else
				throw TException.INVALID_REQUEST_PARAMETERS;
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return new HashMap<String, Object>();
	}
	
	private Map<String, Object> pushBroadCast(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.BROAD_CAST)
					&& request.containsKey(ReqBaseGameApi.VERSION)) {
				String broadcast = String.valueOf(request.get(ReqBaseGameApi.BROAD_CAST));
				String version = String.valueOf(request.get(ReqBaseGameApi.VERSION));
				game.sendBroadcast(version, broadcast);
				response.put(ResBaseGameApi.DATA, true);
				return response;
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> manualPurchase(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.USER_NAME)
					&& request.containsKey(ReqBaseGameApi.MONEY)
					&& request.containsKey(ReqBaseGameApi.REASON)) {
				String userName = String.valueOf(request.get(ReqBaseGameApi.USER_NAME));
				int money = Integer.parseInt(String.valueOf(request.get(ReqBaseGameApi.MONEY)));
				String reason = String.valueOf(request.get(ReqBaseGameApi.REASON));
				response.put(ResBaseGameApi.DATA, game.manualPurchase(userName, money, reason));
				return response;
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> deletePlayer(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.USER_NAME)) {
				String userName = String.valueOf(request.get(ReqBaseGameApi.USER_NAME));
				playerApi.logOut(userName);
				playerDao.delete(userName);
				response.put(ResBaseGameApi.DATA, true);
				return response;
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
	private Map<String, Object> kickPlayer(Map<String, Object> request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			if (request.containsKey(ReqBaseGameApi.USER_NAME)) {
				String userName = String.valueOf(request.get(ReqBaseGameApi.USER_NAME));
				playerApi.logOut(userName);
				response.put(ResBaseGameApi.DATA, true);
				return response;
			} else {
				throw TException.INVALID_REQUEST_PARAMETERS;
			}
		} catch (InvalidRequestParametersException e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
		return response;
	}
	
}
