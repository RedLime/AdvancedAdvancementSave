package com.redlimerl.advancementsave;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdvancedAdvancementSave {
    public static final Set<String> UPDATING_SETS = Sets.newConcurrentHashSet();
    public static final Map<UUID, Runnable> UPDATED_PLAYER_MAP = Maps.newConcurrentMap();
    public static final ExecutorService THREAD_EXECUTOR = Executors.newFixedThreadPool(4);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
