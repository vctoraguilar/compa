package com.rasgo.compa.utils;

import android.content.Context;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.UploadAttachmentsNetworkType;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;


public class GetStream {

    public final ChatClient client;

    public GetStream(Context context){
        StreamStatePluginFactory streamStatePluginFactory = new StreamStatePluginFactory(
                new StatePluginConfig(true, true), context
        );

        // Configuración del plugin de almacenamiento offline
        StreamOfflinePluginFactory streamOfflinePluginFactory = new StreamOfflinePluginFactory(
                context
        );

        // Inicialización del ChatClient con los plugins necesarios
        client = new ChatClient.Builder("7r7sx9khusmb", context)
                .withPlugins(streamOfflinePluginFactory, streamStatePluginFactory)
                .logLevel(ChatLogLevel.ALL) // Cambiar a ChatLogLevel.NOTHING en producción
                .build();
    }

}