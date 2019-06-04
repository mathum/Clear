package com.clearcrane.databean;

import java.io.InputStream;
import java.util.List;

public interface UpDateParser {
	     /** 
	     * 解析输入流 得到Book对象集合 
	     * @param is 
	     * @return 
	     * @throws Exception 
	     */
	public List<UpDate> parse(InputStream is) throws Exception;
	
	     /** 
	     * 序列化Book对象集合 得到XML形式的字符串 
	     * @param books 
	     * @return 
	     * @throws Exception 
	     */
	public String serialize(List<UpDate> updatas) throws Exception;
}
