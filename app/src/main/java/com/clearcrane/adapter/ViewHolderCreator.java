package com.clearcrane.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ViewHolderCreator<T> {

	private final Constructor<?> mConstructor;
	private Object[] mInstanceObjects;

	private ViewHolderCreator(Constructor<?> mConstructor, Object[] mInstanceObjects) {
		this.mConstructor = mConstructor;
		this.mInstanceObjects = mInstanceObjects;
	}

	public static <T> ViewHolderCreator<T> create(final Object enclosingInstance, final Class<?> clazz,
                                                  final Object... args) {

		if (clazz == null) {
			throw new IllegalArgumentException("---- ViewHolder is null -----");
		}

		// top class
		boolean isEnclosingInstanceClass = false;

		if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
			isEnclosingInstanceClass = true;
		}

		// inner instance class should pass enclosing class,so +1
		int argsLength = isEnclosingInstanceClass ? args.length + 1 : args.length;

		final Object[] instanceObject = new Object[argsLength];

		int copyStart = 0;

		// if it is inner instance class,first argument should be the
		// enclosing class instance

		if (isEnclosingInstanceClass) {
			instanceObject[0] = enclosingInstance;
			copyStart = 1;
		}

		// has copy construction parameters
		if (args.length > 0) {
			System.arraycopy(args, 0, instanceObject, copyStart, args.length);
		}

		// fill the types
		final Class<?>[] parameterTypes = new Class[argsLength];
		for (int i = 0; i < instanceObject.length; i++) {
			parameterTypes[i] = instanceObject[i].getClass();
		}

		Constructor<?> constructor = null;

		try {
			constructor = clazz.getDeclaredConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		if (constructor == null) {
			throw new IllegalArgumentException("ViewHolderClass can not be initiated");
		}

		return new ViewHolderCreator<T>(constructor, instanceObject);
	}
	
	@SuppressWarnings("unchecked")
	public ViewHolderBase<T> createViewHolder(int position){
		Object object = null;
		
		try{
			boolean isAccessible = mConstructor.isAccessible();
			if(!isAccessible){
				mConstructor.setAccessible(true);
			}
			
//			Returns a new instance of the declaring class, initialized by dynamically
//		    invoking the constructor represented by this object.
			object = mConstructor.newInstance(mInstanceObjects);
		
		if(!isAccessible){
			mConstructor.setAccessible(false);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	
		if(object == null || !(object instanceof ViewHolderBase)){
			throw new IllegalArgumentException("ViewHolderClass can not be initiated");
		}

		return (ViewHolderBase<T>)object;
		
	}
}
