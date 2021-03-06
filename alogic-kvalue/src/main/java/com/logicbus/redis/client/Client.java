package com.logicbus.redis.client;

import java.lang.reflect.Constructor;

import com.anysoft.pool.CloseAware;
import com.anysoft.pool.PooledCloseable;
import com.logicbus.redis.util.RedisException;


/**
 * Redis客户端
 * @author duanyy
 *
 * @version 1.0.0.1 [20141106 duanyy] <br>
 * - 修正设置index或password之后死循环的bug. <br>
 * 
 */
public class Client extends Connection implements PooledCloseable<Client>{
	
	/**
	 * 当前数据库 
	 */
	private int db = 0;
	
	/**
	 * 设置当前DB
	 * @param dbIndex 
	 */
	public void setCurrentDB(final int dbIndex){
		db = dbIndex;
	}
	
	/**
	 * 获取当前DB
	 * @return
	 */
	public long getCurrentDB(){
		return db;
	}
	
	/**
	 * 验证密码
	 */
	private String password;
	
	/**
	 * 设置验证密码
	 * @param pwd
	 */
	public void setPassword(final String pwd){
		password = pwd;
	}
	
	public Client(final String host) {
		super(host);
	}

	public Client(final String host, final int port) {
		super(host, port);
	}
	
	public Client(final String host, final int port, final int dbIndex){
		super(host, port);
		db = dbIndex;
	}
	
	public Client(final String host, final int port, final String pwd){
		super(host, port);
		password = pwd;
	}
	
	public Client(final String host, final int port, final String pwd, final int dbIndex){
		super(host, port);
		db = dbIndex;
		password = pwd;
	}
	
	
	
	public void connect() {
		super.connect();
		if (password != null && password.length() > 0) {
			auth(password);
		}
		if (db > 0) {
			select(db);
		}
	}
	
	
	public void disconnect() {
		db = 0;
		if (isConnected(false)){
			// to ask the server to close
			try {
				quit();
			}catch (Exception ex){
				
			}
		}
		super.disconnect();
	}
	
	
	public void poolClose() {
		if (closeAware != null){
			closeAware.closeObject(this);
		}
	}

	protected CloseAware<Client> closeAware = null;
	
	
	public void register(CloseAware<Client> listener) {
		closeAware = listener;
	}

	
	public void unregister(CloseAware<Client> listener) {
		closeAware = null;
	}		
	
	/**
	 * to create a toolkit using clazz
	 * @param clazz 工
	 * @return
	 */
	public Toolkit getToolKit(Class<? extends Toolkit> clazz){
		try {
			Constructor<? extends Toolkit> c = clazz.getConstructor(Connection.class);
			return c.newInstance(this);
		}catch (Exception e){			
			throw new RedisException("client","can not get toolkit",e);
		}
	}
}
