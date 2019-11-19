package com.clearcrane.databean;

import java.util.List;

/**
 * Created by mathum
 * on 2019/11/18.
 */
public class ChannelBean {


    /**
     * project : projectName
     * channel_id : 2
     * name : 12121211
     * version_num : 8
     * programs : [{"isSrollTxt":0,"regions":[{"region_id":3,"type_id":2,"top":0,"height":1080,"width":1920,"left":0}],"repeat":127,"name":"12312312321321312","start_time":"2019-11-18 00:00:00","height":1080,"priority":1,"width":1920,"segments":[{"start_time":"15:35:47","materials":[{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/77ed36f4b18679ce54d4cebda306117e_157405944198.jpg","duration":30,"name":"timg.jpg"},{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/9e02a875670f10fc0407846a04af687a_157405944194.jpg","duration":30,"name":"timg (2).jpg"},{"region_id":3,"type_id":8,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/b885388c4991599be65d85f65fc06b12_157405944323.mp3","duration":261,"name":"薛之谦 - 演员.mp3"}],"end_time":"16:41:09"}],"end_time":"2020-11-18 23:59:59","background":"http://192.168.0.15/nativevod/resource/18917fc9102bcc934513a5551d238096_14597637072.jpg","scroll_txt":{"content":"","sroll_style":""},"id":3}]
     */

    private String project;
    private int channel_id;
    private String name;
    private int version_num;
    private List<ProgramsBean> programs;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(int channel_id) {
        this.channel_id = channel_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion_num() {
        return version_num;
    }

    public void setVersion_num(int version_num) {
        this.version_num = version_num;
    }

    public List<ProgramsBean> getPrograms() {
        return programs;
    }

    public void setPrograms(List<ProgramsBean> programs) {
        this.programs = programs;
    }

    public static class ProgramsBean {
        /**
         * isSrollTxt : 0
         * regions : [{"region_id":3,"type_id":2,"top":0,"height":1080,"width":1920,"left":0}]
         * repeat : 127
         * name : 12312312321321312
         * start_time : 2019-11-18 00:00:00
         * height : 1080
         * priority : 1
         * width : 1920
         * segments : [{"start_time":"15:35:47","materials":[{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/77ed36f4b18679ce54d4cebda306117e_157405944198.jpg","duration":30,"name":"timg.jpg"},{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/9e02a875670f10fc0407846a04af687a_157405944194.jpg","duration":30,"name":"timg (2).jpg"},{"region_id":3,"type_id":8,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/b885388c4991599be65d85f65fc06b12_157405944323.mp3","duration":261,"name":"薛之谦 - 演员.mp3"}],"end_time":"16:41:09"}]
         * end_time : 2020-11-18 23:59:59
         * background : http://192.168.0.15/nativevod/resource/18917fc9102bcc934513a5551d238096_14597637072.jpg
         * scroll_txt : {"content":"","sroll_style":""}
         * id : 3
         */

        private int isSrollTxt;
        private int repeat;
        private String name;
        private String start_time;
        private int height;
        private int priority;
        private int width;
        private String end_time;
        private String background;
        private ScrollTxtBean scroll_txt;
        private int id;
        private List<RegionsBean> regions;
        private List<SegmentsBean> segments;

        public int getIsSrollTxt() {
            return isSrollTxt;
        }

        public void setIsSrollTxt(int isSrollTxt) {
            this.isSrollTxt = isSrollTxt;
        }

        public int getRepeat() {
            return repeat;
        }

        public void setRepeat(int repeat) {
            this.repeat = repeat;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public ScrollTxtBean getScroll_txt() {
            return scroll_txt;
        }

        public void setScroll_txt(ScrollTxtBean scroll_txt) {
            this.scroll_txt = scroll_txt;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<RegionsBean> getRegions() {
            return regions;
        }

        public void setRegions(List<RegionsBean> regions) {
            this.regions = regions;
        }

        public List<SegmentsBean> getSegments() {
            return segments;
        }

        public void setSegments(List<SegmentsBean> segments) {
            this.segments = segments;
        }

        public static class ScrollTxtBean {
            /**
             * content :
             * sroll_style :
             */

            private String content;
            private String sroll_style;

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getSroll_style() {
                return sroll_style;
            }

            public void setSroll_style(String sroll_style) {
                this.sroll_style = sroll_style;
            }
        }

        public static class RegionsBean {
            /**
             * region_id : 3
             * type_id : 2
             * top : 0
             * height : 1080
             * width : 1920
             * left : 0
             */

            private int region_id;
            private int type_id;
            private int top;
            private int height;
            private int width;
            private int left;

            public int getRegion_id() {
                return region_id;
            }

            public void setRegion_id(int region_id) {
                this.region_id = region_id;
            }

            public int getType_id() {
                return type_id;
            }

            public void setType_id(int type_id) {
                this.type_id = type_id;
            }

            public int getTop() {
                return top;
            }

            public void setTop(int top) {
                this.top = top;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getLeft() {
                return left;
            }

            public void setLeft(int left) {
                this.left = left;
            }
        }

        public static class SegmentsBean {
            /**
             * start_time : 15:35:47
             * materials : [{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/77ed36f4b18679ce54d4cebda306117e_157405944198.jpg","duration":30,"name":"timg.jpg"},{"region_id":3,"type_id":4,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/9e02a875670f10fc0407846a04af687a_157405944194.jpg","duration":30,"name":"timg (2).jpg"},{"region_id":3,"type_id":8,"scroll_style":[{}],"description":[""],"source_url":"http://192.168.0.15/nativevod/resource/b885388c4991599be65d85f65fc06b12_157405944323.mp3","duration":261,"name":"薛之谦 - 演员.mp3"}]
             * end_time : 16:41:09
             */

            private String start_time;
            private String end_time;
            private List<MaterialsBean> materials;

            public String getStart_time() {
                return start_time;
            }

            public void setStart_time(String start_time) {
                this.start_time = start_time;
            }

            public String getEnd_time() {
                return end_time;
            }

            public void setEnd_time(String end_time) {
                this.end_time = end_time;
            }

            public List<MaterialsBean> getMaterials() {
                return materials;
            }

            public void setMaterials(List<MaterialsBean> materials) {
                this.materials = materials;
            }

            public static class MaterialsBean {
                /**
                 * region_id : 3
                 * type_id : 4
                 * scroll_style : [{}]
                 * description : [""]
                 * source_url : http://192.168.0.15/nativevod/resource/77ed36f4b18679ce54d4cebda306117e_157405944198.jpg
                 * duration : 30
                 * name : timg.jpg
                 */

                private int region_id;
                private int type_id;
                private String source_url;
                private int duration;
                private String name;
                private List<ScrollStyleBean> scroll_style;
                private List<String> description;

                public int getRegion_id() {
                    return region_id;
                }

                public void setRegion_id(int region_id) {
                    this.region_id = region_id;
                }

                public int getType_id() {
                    return type_id;
                }

                public void setType_id(int type_id) {
                    this.type_id = type_id;
                }

                public String getSource_url() {
                    return source_url;
                }

                public void setSource_url(String source_url) {
                    this.source_url = source_url;
                }

                public int getDuration() {
                    return duration;
                }

                public void setDuration(int duration) {
                    this.duration = duration;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<ScrollStyleBean> getScroll_style() {
                    return scroll_style;
                }

                public void setScroll_style(List<ScrollStyleBean> scroll_style) {
                    this.scroll_style = scroll_style;
                }

                public List<String> getDescription() {
                    return description;
                }

                public void setDescription(List<String> description) {
                    this.description = description;
                }

                public static class ScrollStyleBean {
                }
            }
        }
    }
}
