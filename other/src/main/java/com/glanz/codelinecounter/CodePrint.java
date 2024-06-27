package com.glanz.codelinecounter;

public class CodePrint {
    /**
     * 代码行数统计
     */
    public static void main(String[] args) {
        LineCounter.CountResult result = new LineCounter.Builder("G:\\Code\\githubRepository\\commonutils\\other\\src\\main\\java\\com\\glanz") // 可以有多个
                .suffix(".java", ".txt", ".xml") // 要统计的后缀，留空则统计所有
                .exclude(".idea", "build", "generated") // 忽略的文件/文件夹
                .granularity(LineCounter.Granularity.TYPE) // 统计的粒度，统计结果以文件/类型/总计为单位，默认TYPE
                .strict(true) // 是否过滤无效行，默认否
                .filter(null) // 过滤无效行规则，留空则使用默认规则
                .name("default name") // 当前统计器名称，在打印时使用
                .printDetail(false) // 打印时是否打印具体统计结果HashMap，默认否
                .printAfterCount(true) // 是否在统计之后打印，默认是
                .countUseTime(true) // 是否打印统计用时，默认否
                .count(); // 创建对象并进行统计
    }
}
