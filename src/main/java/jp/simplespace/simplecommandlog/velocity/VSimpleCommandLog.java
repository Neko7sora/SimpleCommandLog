package jp.simplespace.simplecommandlog.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import jp.simplespace.simplecommandlog.ConfigData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Plugin(id = "simplecommandlog", name = "SimpleCommandLog", version = "1.4",
        url = "https://simplespace.jp", description = "シンプルなコマンドログ", authors = {"Matchan"})
public class VSimpleCommandLog {
    private static ProxyServer server;
    private static Logger logger;
    private static Path dataDirectory;
    private static File config;
    private static Yaml yaml;
    private static ConfigData configData;
    public static TextComponent prefix = Component.text().append(Component.text("[VSCL] ",NamedTextColor.AQUA)).append(Component.text("",NamedTextColor.WHITE)).build();
    public static TextComponent noPermission = Component.text().append(prefix).append(Component.text("あなたに実行する権限はありません。",NamedTextColor.RED)).append(Component.text("",NamedTextColor.WHITE)).build();

    @Inject
    public VSimpleCommandLog(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        VSimpleCommandLog.server = server;
        VSimpleCommandLog.logger = logger;
        VSimpleCommandLog.dataDirectory = dataDirectory;

        logger.info("SimpleCommandLogが読み込まれました！");
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        yaml = new Yaml(new CustomClassLoaderConstructor(VSimpleCommandLog.class.getClassLoader()));
        saveDefaultConfig();
        configData = getNewConfigData();
        CommandManager commandManager = server.getCommandManager();
        CommandMeta cmdlogMeta = commandManager
                .metaBuilder("vscl")
                .aliases("vcmdlog","vcl")
                .build();
        commandManager.register(cmdlogMeta,new VCmdLog());
        server.getEventManager().register(this, new VCmdLog());
        CommandMeta evalMeta = commandManager
                .metaBuilder("veval")
                .build();
        commandManager.register(evalMeta,new VEval());
    }
    public static ProxyServer getServer(){
        return server;
    }
    public static Logger getLogger(){
        return logger;
    }
    public static File getConfig(){
        return new File(dataDirectory.toFile(),"config.yml");
    }
    public static ConfigData getNewConfigData(){
        try {
            return yaml.loadAs(new FileReader(getConfig()),ConfigData.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void saveConfig(ConfigData configData){
        String str = yaml.dump(configData);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(config),StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ConfigData getConfigData(){
        return configData;
    }
    public static void saveDefaultConfig(){
        if(!dataDirectory.toFile().exists()) dataDirectory.toFile().mkdir();
        config = getConfig();
        if(!config.exists()){
            try {
                config.createNewFile();
                InputStream is = VSimpleCommandLog.class.getResourceAsStream("/config.yml");
                InputStreamReader ir = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ir);
                FileWriter fw = new FileWriter(config);
                BufferedWriter bw = new BufferedWriter(fw);
                StringBuilder sb = new StringBuilder();
                String str = br.readLine();
                while(str != null){
                    sb.append(str).append("\n");
                    str = br.readLine();
                }
                bw.write(sb.toString());
                bw.close();
                fw.close();
                br.close();
                ir.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
