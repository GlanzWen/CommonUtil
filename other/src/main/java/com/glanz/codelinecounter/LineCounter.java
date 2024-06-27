package com.glanz.codelinecounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("ALL")
public class LineCounter {
    public static final String KEY_TOTAL = "total";

    /**
     * 需要统计的文件夹。
     */
    private final List<String> paths;
    /**
     * 需要统计的文件格式名；如果留空，则统计全部。
     */
    private final Set<String> suffixes;
    private final Set<String> excludes;

    /**
     * 是否开启过滤。如果设置为true，那么将过滤掉无效的行数，默认过滤空白行和注释，并分别将空白行数、注释行数保存在
     * {@link CountResult#spaceLine}和{@link CountResult#commentLine}中。
     * 使用{@link LineCounter#customFilter}来自定义过滤规则。
     *
     * @see LineCounter#customFilter
     */
    private final boolean strict;

    /**
     * 自定义过滤器，选择合法的行。使用{@link Builder#filter(Predicate)}定义。
     *
     * @see Builder
     */
    private Predicate<String> customFilter;

    /**
     * 统计的粒度，FILE表示最终记录每个文件的行数，TYPE表示记录每种格式的文件的总行数，TOTAL表示记录所有文件总行数
     */
    private Granularity granularity;

    public enum Granularity {
        FILE, TYPE, TOTAL
    }

    private String counterName;
    private boolean printDetail;
    private boolean printAfterCount;
    private boolean countUseTime;

    public LineCounter(Builder builder) {
        this.paths = builder.paths;
        this.suffixes = builder.suffixes == null ? new HashSet<>() : builder.suffixes;
        this.excludes = builder.excludes == null ? new HashSet<>() : builder.excludes;
        this.granularity = builder.granularity;
        this.strict = builder.strict;
        this.customFilter = builder.filter;
        this.counterName = builder.counterName;
        this.printDetail = builder.printDetail;
        this.printAfterCount = builder.printAfterCount;
        this.countUseTime = builder.countUseTime;
    }

    /**
     * 统计行数，返回一个{@link CountResult}对象。
     */
    public CountResult count() {
        CountResult result = new CountResult();
        long startTime = System.currentTimeMillis();
        for (String path : paths) {
            count(new File(path), result);
        }
        long endTime = System.currentTimeMillis();
        if (countUseTime) {
            result.useTime = (int) (endTime - startTime);
        }
        if (printAfterCount) {
            result.print();
            System.out.println();
        }
        return result;
    }

    private void count(File file, CountResult result) {
        String filename = file.getName();
        if (excludes.contains(filename)) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                System.err.println("An error occurs while getting children files on directory [" + file.getAbsolutePath() + "].");
                return;
            }
            for (File child : children) {
                count(child, result);
            }
        } else {
            String suffix;
            if (filename.lastIndexOf(".") == -1) suffix = filename;
            else suffix = filename.substring(filename.lastIndexOf("."));
            if (!suffixes.isEmpty() && !suffixes.contains(suffix)) return;
            result.fileCount++;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                int lineNum = strict ? (int) reader.lines()
                        .filter(line -> {
                            if (customFilter != null) {
                                return customFilter.test(line);
                            }
                            line = line.trim();
                            if (line.length() == 0) {
                                // 空白行
                                result.spaceLine++;
                                return false;
                            }
                            if (line.startsWith("//") ||
                                    line.startsWith("/*") ||
                                    line.startsWith("*") ||
                                    line.startsWith("<!--")) {
                                // 注释行
                                result.commentLine++;
                                return false;
                            }
                            return true;
                        }).count()
                        : (int) reader.lines().count();
                result.addResult(file, lineNum, suffix);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 统计结果
     */
    public class CountResult {
        private final Map<String, Integer> countMap;
        /**
         * 统计文件数
         */
        private int fileCount;
        /**
         * 空白行数
         */
        private int spaceLine;
        /**
         * 注释行数
         */
        private int commentLine;
        /**
         * 统计用时
         */
        private int useTime;

        CountResult() {
            countMap = new HashMap<>();
            fileCount = 0;
            spaceLine = 0;
            commentLine = 0;
        }

        void addResult(File file, int lineNum, String suffix) {
            switch (granularity) {
                case FILE:
                    countMap.put(file.getAbsolutePath(), lineNum);
                    break;
                case TYPE:
                    countMap.merge(suffix, lineNum, Integer::sum);
                    break;
                case TOTAL:
                    countMap.merge(KEY_TOTAL, lineNum, Integer::sum);
                    break;
            }
        }

        public void print() {
            System.out.println("------ " + counterName + " ------");
            int totalLines = countMap.values()
                    .stream()
                    .mapToInt(value -> value)
                    .sum();
            StringBuilder sb = new StringBuilder();
            sb.append("总文件数：").append(fileCount);
            if (strict) {
                sb.append("，有效代码行数：").append(totalLines)
                        .append("，空白行数：").append(spaceLine)
                        .append("，注释行数：").append(commentLine);
            } else {
                sb.append("，代码行数：").append(totalLines);
            }
            if (printDetail) {
                sb.append("，详情：").append(countMap);
            }
            if (countUseTime) {
                sb.append("，用时：").append(useTime).append("ms");
            }
            System.out.println(sb);
        }
    }

    public static class Builder {
        private List<String> paths;
        private Set<String> suffixes = null;
        private Set<String> excludes = null;
        private Granularity granularity = Granularity.TYPE;
        private boolean strict = false;
        private String counterName = "Unnamed";
        private Predicate<String> filter = null;
        private boolean printDetail = false;
        private boolean printAfterCount = true;
        private boolean countUseTime;

        public Builder(String path) {
            this.paths = new ArrayList<>();
            this.paths.add(path);
        }

        public Builder(String... path) {
            this.paths = new ArrayList<>(Arrays.asList(path));
        }

        public Builder(Collection<String> paths) {
            this.paths = new ArrayList<>(paths);
        }

        public Builder suffix(String... suffix) {
            return suffix(Arrays.asList(suffix));
        }

        public Builder suffix(Collection<String> suffix) {
            this.suffixes = new HashSet<>(suffix);
            return this;
        }

        public Builder exclude(String... excludes) {
            return exclude(Arrays.asList(excludes));
        }

        public Builder exclude(Collection<String> excludes) {
            this.excludes = new HashSet<>(excludes);
            return this;
        }

        public Builder granularity(Granularity g) {
            this.granularity = g;
            return this;
        }

        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder filter(Predicate<String> filter) {
            this.filter = filter;
            return this;
        }

        public Builder name(String str) {
            this.counterName = str;
            return this;
        }

        public Builder printDetail(boolean printDetail) {
            this.printDetail = printDetail;
            return this;
        }

        public Builder printAfterCount(boolean b) {
            this.printAfterCount = b;
            return this;
        }

        public Builder countUseTime(boolean count) {
            this.countUseTime = count;
            return this;
        }

        public LineCounter build() {
            return new LineCounter(this);
        }

        public CountResult count() {
            return build().count();
        }
    }
}

