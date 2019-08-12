package com.clearcrane.constant;


public class ClearConstant {
	
	public static final String STR_COUNT = "count";
	
	public static final int MSG_APP_RESTART = 13;
	//***************以下为直播*****************************
	//数字键输入数字超时时间
	public static final int LIVE_INSERT_OVERTIME = 2000;
	
	//遥控器快速操作时间阀值
	public static final int LIVE_QUICK_INSERT_TIME = 500;
	
	//快速操作结束后delay执行时间
	public static final int LIVE_QUICK_INSERT_DELAY = 800;
	
	//提示框显示时间多久后消失
	public static final int LIVE_PROMET_SHOW_LENGTH = 2400;
	
	//输入数字显示超时时间
	public static final int LIVE_INSERT_SHOW_TIME = 3000;
	
	//**************以下为升级******************************
	//是否在升级标志
	public static final String PREF_UPDATE_STARTED = "update_started";
	//本地升级url
	public static final String LOCAL_UPDATE_SERVER = "local_update_server";
	//云升级url
	public static final String CLOUD_UPDATE_SERVER = "cloud_update_server";
	//主页地址
	public static final String MAIN_SERVER = "main_server";
	
	public static final String MAIN_SERVER_IP = "main_server_ip";
	
	public static final String BACKUP_SERVER = "backup_server";
	//
	public static final String BACKGROUND_VIDEO_URL = "background_video";

	public static final String PREF_LOG_SERVER = "cloud_log_server";
	
	public static final String PREF_PLAY_LOG_SERVER = "play_log_server";
	
	public static final String ROOM_ID = "room_id";
	
	//间隔十分钟
	public final static int UPDATE_APK_DURATION = 10 * 60 * 1000;
	
	//**************滚动字幕******************************
	public static final String SCROLLTEXT_FILE = "commandText";
	
	public static final String SCROLLTEXT_VERSION = "commandVersion";
	
	public static final String SCROLLTEXT_CONTENT = "commandContent";
	
	public static final String SCROLLTEXT_TIME_START = "start_time";
	
	public static final String SCROLLTEXT_TIME_END = "end_time";
	
	public static final String SCROLLTEXT_TITLE = "title";
	
	public static final String SCROLLTEXT_EXPIREDTIME = "commandExpiredTime";
	
	public static final String SCROLLTEXT_TYPE = "commandType";
	
	public static final String SCROLLTEXT_SERVER = "commandServer";
	
	//**************当前进行活动**********************************
	public static final String VIDEOSETS = "video_sets";
	
	public static final String VIDEOSETSJSON = "video_sets_json";
	//**************当前进行活动**********************************
    public static final String Activity_FILE = "activityText";
	
	
    public static final String Play_Statue ="playStatue";
    public static final String Movie_NAME ="movieName";
    
    public static final String VIEW_NAME ="viewName";
    
	public static final String Activity_VERSION ="activityVersion";
	
	//**************插播**********************************
    public static final String INTERCUT_FILE = "interCutText";
	
    public static final String INTERCUT_TITLE = "interCutTitle";
    
	public static final String ORIGIN_TIME = "orginTime";
	
	public static final String TERMINATE_TIME = "terminateTime";
	
	public static final String INTERCUT_TYPE = "type";
	
	public static final String INTERCUT_URL = "url";
	
	public static final String INTERCUT_PLAYINGTIME ="playingTime";
	
	public static final String INTERCUT_VERSION ="intercutVersion";

	public static final String APP_STATUS ="app_status";
	
	
	//**************可用时间**********************************
	public static final String ACCESSTIME_FILE = "accessTimeText";
	
	public static final String START_TIME = "startTime";
	
	public static final String END_TIME = "endTime";
	
	public static final String ACCESSTIME_VERSION ="accessTimeVersion";
	
	
	//**************模块群组**********************************
	public static final String MODULEGROUP_FILE = "moduleGroupText";
	public static final String ModuleGroup_ID = "moduleGroupId";
	public static final String CHANNEL_FILE = "channelFile";
	public static final String CHANNEL_ID = "channelId";
	public static final String CHANNEL_VERSION = "channelVersion";
	
	public static final String MODULEGROUP_VERSION ="moduleGroupVersion";
	
	//**************留言**********************************
	public static final String MESSAGE_FILE = "messageText";
	
	
	public static final String MESSAGE_JSON = "messageJson";
	
	public static final String MESSAGE_NEWSETVERSION = "newestVersion";
	
	//**************字体**********************************
	public static final String TEXT_FONT_FOR_NIGERIA_PATH = "fonts/HelveticaNeueLTPro-Roman.ttf";
	
	//**************重启**********************************reboot_enable
	public static final String REBOOT_ENABLE = "reboot_isenable";
	
	public static final String REBOOT_HOUR = "reboot_hour";
	
	public static final String REBOOT_MINUTE = "reboot_minute";
	
	public static final int[] MINUTE = {00, 15, 30, 45};
	
	//**************设置**********************************
//	public static String SETTING_KEYS = "007";
	public static String SETTING_KEYS = "911";
	public static final String APPKEYS = "6321";
	//**************播放计划*****************************
	public static int MODE_PRISON_VOD = 0;
	public static int MODE_PRISON_IMS = 1;
	
	public final static int JUMP_LIVE_VIEW = 413;
	public final static int JUMP_MOVIE_VIEW = 414;
	
	public final static int SUB_VIEW_IAMGEVIEW_WIDTH = 300;
	public final static int SUB_VIEW_IAMGEVIEW_HEIGHT = 300;
	
	public static int screenWidth;
    public static int screenHeight;
    
    final public static int TEMP_BUF_LEN = 10 * 1024; // 10KB

    public static String ResourceDir = "resource";
	public static final String FileName = "playlist.txt";
	public static final String CURRENT_APP_STATUS = "current_app_status";
	public static final String INSTALL_STATUS = "install_status";
	
	
	 /**
     * 0 为正常模式, 1为非可用时间黑屏状态, 2为插播状态, 3为计划播状态。
     */
	public static final int CODE_VOD_STATE = 0;
	public static final int CODE_ACCESS_TIME_STATE = 1; //in fact is not access
	public static final int CODE_INTER_CUT_STATE = 2;
	public static final int CODE_CHANNEL_STATE = 3;
	public static final int CODE_TERM_FORCED_STATE = 4;
	
	/*
	 * version common
	 */
	public static final String STR_CURRENT_VERSION = "curVersion";
	public static final String STR_CONTENT = "content";
	public static final String STR_ACCESSTIME_CONTENT = "accesstime_content";
	public static final String STR_START_TIME = "start_time";
	public static final String STR_END_TIME = "end_time";
	public static final String STR_TITLE = "title";
	public static final String STR_SCROLL_STYLE = "scroll_style";
	public static final String STR_COLOR = "color";
	public static final String STR_INTERVAL = "interval";
	public static final String STR_LOCATION = "location";
	public static final String STR_FONT_FAMILY = "font_family";
	public static final String STR_FONT_SIZE = "fontSize";
	public static final String STR_TYPE_DIRECTION = "direction";
	public static final String STR_NEWEST_VERSION = "newestversion";
	public static final String STR_COMMAND_CONTENT = "commandContent";
	public static final String STR_SOURCE_URL = "source_url";
	public static final String STR_TYPE = "type";
	public static final String STR_TIME_INTERVAL= "time_interval";
	public static final String STR_MATERIAL_NAME= "material_name";
	public static final String STR_DURATION= "duration";
	public static final String URL_BACKEND_PREFIX = "http://192.168.0.62";
	public static final String STR_BACKEND_PORT = ":8000";
	
	
	/*
	 * scrolltext
	 */
	public static final String STR_SCROLL_TEXT = "scrolltext";
	public static final String URL_SCROLL_TEXT_SUFFIX = "/backend/GetScrollText";
	
	/*
	 * intercut
	 */
	public static final String STR_INTER_CUT = "intercut";
	public static final String URL_INTER_CUT_SUFFIX = "/backend/GetInterCut";

	/*
	 * state intercut
	 */
	public static final String STR_STATE_INTER_CUT = "stateIntercut";
	public static final String URL_STATE_INTER_CUT_SUFFIX = "/backend/GetStateInterCut";
	
	/*
	 * channel
	 */
	public static final String STR_CHANNEL = "channel";
	public static final String URL_CHANNEL_SUFFIX = "/backend/GetChannel";
	public static final String STR_CHANNEL_ID = "channel_id";
	
	
	/*
	 * accesstime
	 */
	public static final String STR_ACCESS_TIME = "accesstime";
	public static final String URL_ACCESS_TIME_SUFFIX = "/backend/GetAccessTime";
	public static final String STR_ACCESS_TIME_CN = "系统以下时间段可用:";
	public static final String STR_ACCESS_TIME_ARRAY = "accesstime_array";
	
	
	/*
	 * module group
	 * 
	 */
	public static final String STR_MODULE_GROUP = "modulegroup";
	public static final String URL_MODULE_GROUP_SUFFIX = "/backend/GetModuleGroup";
	public static final String STR_MODULE_GROUP_ID = "group_id";
	
	/*
	 * term forced
	 * 
	 */
	public static final String STR_TERM_FORCED = "termforced";
	public static final String URL_TERM_FORCED_SUFFIX = "/backend/TermForced/";
	public static final String STR_TERM_IS_FORCED = "is_forced";
	
	
	/*
	 * update_mainmenu
	 */
	public static final String STR_UPDATE_MAINMENU = "update_mainmenu";
	
	public static final String STR_SNAP_SHOT = "snapshot";
	public static final String STR_VOLUME = "volume";
	public static final String STR_RESCODE = "rescode";
	
	/*
	 * state msg code
	 * 
	 */
	public static final int MSG_START_STATE_INTER_CUT = 315;
	public static final int MSG_STOP_STATE_INTER_CUT = 316;
	public static final int MSG_START_INTER_CUT = 301;
	public static final int MSG_STOP_INTER_CUT = 302;
	public static final int MSG_START_CHANNEL = 303;
	public static final int MSG_STOP_CHANNEL = 304;
	public static final int MSG_START_ACCESS_TIME = 305; //show black view
	public static final int MSG_STOP_ACCESS_TIME = 306; //hide
	public static final int MSG_INTERRUPT_MUSIC_START = 307;
	public static final int MSG_INTERRUPT_MUSIC_STOP = 308;		
    public static final int MSG_INTERRUPT_PICTURE_START = 309;
    public static final int MSG_INTERRUPT_PICTURE_STOP = 310;
    public static final int MSG_START_CHANNEL_LIST = 311;
    public static final int MSG_STOP_CHANNEL_LIST = 312;
    public static final int MSG_SET_TERM_FORCED = 313;
    public static final int MSG_SET_TERM_FREE = 314;
	public static final String STR_SERVER_TIME = "ServerTime";

	public static final String STR_REGION_ID = "region_id";
	
	
	public static final int PROGRAM_WIDGET_TYPE_VIDEO = 1;
	public static final int PROGRAM_WIDGET_TYPE_IMAGE = 2;
	public static final int PROGRAM_WIDGET_TYPE_MUSIC = 3;
	
	//***************以下为音乐播放器*****************************
	public static final int PLAY_MSG = 1;		//播放
	public static final int PAUSE_MSG = 2;		//暂停
	public static final int STOP_MSG = 3;		//停止
	public static final int CONTINUE_MSG = 4;	//继续
	public static final int PRIVIOUS_MSG = 5;	//上一首
	public static final int NEXT_MSG = 6;		//下一首
	public static final int PROGRESS_CHANGE = 7;//进度改变
	public static final int PLAYING_MSG = 8;	//正在播放
	
	
	public static final String SKYWORTH_368_UPDATELIST = "/data/local/update.list";
	
	public static final String CLEAR_CONFIG_JSON_NAME = "ClearConfig.json";
	
	//3秒间隔，3次1分钟
	public static final int WATCH_DOG_WAIT_TIMES = 30;

	public static final int MSG_WATCH_DOG = 911; 
	
	public static final String STR_FIRST_CONNECT_SERVER = "isFirstConnectToServer";
	
}
