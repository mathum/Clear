package com.clearcrane.util;

import java.util.List;

public interface BeanAdapter {

	// convert server feedback to certain beans
	public Object getBean(Object from, Class<?> to);
	public List<?> getBeanList(Object from, Class<?> to);

}
