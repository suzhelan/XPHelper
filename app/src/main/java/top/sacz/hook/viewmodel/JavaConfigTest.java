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
        strs.add("v1");
        strs.add("v2");
        //simple put
        config.put("strs",strs);
        //obj list
        List<ScenarioPayload> objs = new ArrayList<>();
        objs.add(new ScenarioPayload("wadaw", 1));
        objs.add(new ScenarioPayload("wadwdas", 2));
        config.put("objs", objs);

        //obj
        ScenarioPayload obj = new ScenarioPayload("onlysss", 0);
        config.put("obj", obj);
        //全部转成结果字符串
        return "put(List<String>=[...])" +
                "put(List<ScenarioPayload>=[...])" +
                "put(ScenarioPayload=[...])";
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
