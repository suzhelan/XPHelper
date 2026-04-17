package top.sacz.hook.viewmodel;

import java.util.ArrayList;
import java.util.List;

import top.sacz.hook.entity.ScenarioPayload;
import top.sacz.xphelper.util.ConfigUtils;

public class JavaConfigTest {

    private final ConfigUtils config = new ConfigUtils("jvm");

    public String checkWrite() {
        //string list
        List<String> strs = new ArrayList<>();
        strs.add("1");
        strs.add("2");
        config.putList("strs", strs, String.class);
        //obj list
        List<ScenarioPayload> objs = new ArrayList<>();
        objs.add(new ScenarioPayload("one", 1));
        objs.add(new ScenarioPayload("two", 2));
        config.putList("objs", objs, ScenarioPayload.class);

        //obj
        ScenarioPayload obj = new ScenarioPayload("only", 0);
        config.putObject("obj", obj, ScenarioPayload.class);

        //全部转成结果字符串
        String sb = "putList(List<String>=[\"1\",\"2\"])" +
                "putList(List<ScenarioPayload>=[\"one\",\"two\"])" +
                "putObject(ScenarioPayload=[\"only\",\"0\"])";
        return sb;
    }

    public String readResult() {
        StringBuilder sb = new StringBuilder();
        List<String> strs = config.getList("strs", String.class);
        sb.append("getList<String>=");
        sb.append(strs);
        List<ScenarioPayload> objs = config.getList("objs", ScenarioPayload.class);
        sb.append("getList<ScenarioPayload>=");
        sb.append(objs);
        ScenarioPayload obj = config.getObject("obj", ScenarioPayload.class);
        sb.append("getObject<ScenarioPayload>=");
        sb.append(obj);
        return sb.toString();
    }

}
