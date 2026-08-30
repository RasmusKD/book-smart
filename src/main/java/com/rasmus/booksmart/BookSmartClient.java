package com.rasmus.booksmart;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BookSmartClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BookSmart.init();
        ClientTickEvents.END_CLIENT_TICK.register(CraftAll::tick);
    }
}
