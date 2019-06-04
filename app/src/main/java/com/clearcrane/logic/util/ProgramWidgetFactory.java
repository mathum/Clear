package com.clearcrane.logic.util;

import com.clearcrane.constant.ClearConstant;

public class ProgramWidgetFactory {

	
	public static ProgramBaseWidget createWidget(ProgramLayoutParam param){
		if(param == null)
			return null;
		switch(param.typeId){
		case ClearConstant.PROGRAM_WIDGET_TYPE_IMAGE:
			return new ProgramImageWidget();
		case ClearConstant.PROGRAM_WIDGET_TYPE_VIDEO:
			return new ProgramVideoWidget();
		case ClearConstant.PROGRAM_WIDGET_TYPE_MUSIC:
		    return new ProgramMusicWidget();
		default:
			return null;
		}
	}
}
