package com.github.ioloolo.mcquest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.github.ioloolo.mcquest.command.CommandBase;
import com.github.ioloolo.mcquest.event.EventBase;
import com.github.ioloolo.mcquest.util.ReflectionUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import dev.mccue.guava.reflect.ClassPath;
import dev.sergiferry.playernpc.api.NPCLib;
import lombok.Getter;
import lombok.SneakyThrows;

@SuppressWarnings("unused")
public final class McQuest extends JavaPlugin {

    @Getter
    private static McQuest instance;

    @Override
    @SneakyThrows
    public void onEnable() {
        initDataFolder();
        initFirestore();

        registerInstance();
        registerNpcLib();
        registerCommands();
        registerEvents();
    }

    private void initDataFolder() {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
    }

    @SneakyThrows({FileNotFoundException.class, IOException.class})
    private void initFirestore() {
        File credentialsFile = new File(getDataFolder(), "firebase-credentials.json");

        if (!credentialsFile.exists()) {
            getLogger().warning("Firebase Admin SDK credentials file not found. Please add it to the plugin folder and restart the server. (%s)".formatted(credentialsFile));
            return;
        }

        InputStream serviceAccount = new FileInputStream(credentialsFile);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp.initializeApp(options);
    }

    private void registerInstance() {
        instance = this;
    }

    private void registerNpcLib() {
        NPCLib.getInstance().registerPlugin(instance);
    }

    private void registerCommands() throws IOException {
        ClassPath.from(getClass().getClassLoader())
                .getTopLevelClasses(getClass().getPackageName()+".command")
                .stream()
                .map(classInfo -> ReflectionUtil.clazz(classInfo.getName()))
                .filter(clazz -> clazz.getSuperclass().equals(CommandBase.class))
                .forEach(clazz -> {
                    Object instance = ReflectionUtil.newInstance(clazz);

                    Field commandField = ReflectionUtil.field(CommandBase.class, "command");
                    Field aliasesField = ReflectionUtil.field(CommandBase.class, "aliases");

                    String command = (String) ReflectionUtil.getFieldData(commandField, instance);
                    String[] aliases = (String[]) ReflectionUtil.getFieldData(aliasesField, instance);

                    getServer()
                            .getCommandMap()
                            .register("myquest", new BukkitCommand(command, "", "", List.of(aliases)) {

                                @Override
                                public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                                    if (sender instanceof Player player) {
                                        ReflectionUtil.invoke(
                                                ReflectionUtil.method(CommandBase.class, "onCommand", Player.class, String[].class),
                                                instance,
                                                player,
                                                args
                                        );

                                        return true;
                                    }

                                    return false;
                                }
                            });
                });
    }

    private void registerEvents() throws IOException {
        ClassPath.from(getClass().getClassLoader())
                .getTopLevelClasses(getClass().getPackageName()+".event")
                .stream()
                .map(classInfo -> ReflectionUtil.clazz(classInfo.getName()))
                .filter(clazz -> clazz.getSuperclass().equals(EventBase.class))
                .forEach(clazz -> {
                    Type[] genericTypes = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();

                    //noinspection unchecked
                    Class<? extends Event> eventClass = (Class<? extends Event>)ReflectionUtil.clazz(genericTypes[0].getTypeName());

                    EventBase<?> obj = (EventBase<?>) ReflectionUtil.newInstance(clazz);

                    getServer()
                            .getPluginManager()
                            .registerEvent(
                                    eventClass,
                                    new Listener() {},
                                    obj.getPriority(),
                                    (listener, event) -> {
                                        try {
                                            ReflectionUtil.invoke(
                                                    ReflectionUtil.method(clazz, "onEvent", eventClass),
                                                    obj,
                                                    event
                                            );
                                        } catch (Exception ignored) {}
                                    },
                                    this
                            );

                });
    }
}
