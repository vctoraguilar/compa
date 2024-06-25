package com.rasgo.compa.feature.homepage.chat;

import static java.util.Collections.singletonList;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rasgo.compa.R;
import com.rasgo.compa.databinding.ActivityChatBinding;
import com.rasgo.compa.databinding.FragmentChatBinding;
import com.rasgo.compa.feature.chat.ChatActivity;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;


public class ChatFragment extends Fragment {


    private FragmentChatBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Step 1 - Set up the OfflinePlugin for offline storage
        StreamOfflinePluginFactory streamOfflinePluginFactory = new StreamOfflinePluginFactory(
                requireContext().getApplicationContext()
        );
        StreamStatePluginFactory streamStatePluginFactory = new StreamStatePluginFactory(
                new StatePluginConfig(true, true), requireActivity()
        );

        // Step 2 - Set up the client for API calls with the plugin for offline storage
        ChatClient client = new ChatClient.Builder("7r7sx9khusmb", requireContext().getApplicationContext())
                .withPlugins(streamOfflinePluginFactory, streamStatePluginFactory)
                .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
                .build();

        // Step 3 - Authenticate and connect the user
        User user = new User.Builder()
                .withId("tutorial-droid")
                .withName("Tutorial Droid")
                .withImage("https://bit.ly/2TIt8NR")
                .build();

        client.connectUser(
                user,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.WwfBzU1GZr0brt_fXnqKdKhz3oj0rbDUm2DqJO_SS5U"
        ).enqueue(result -> {
            // Step 4 - Set the channel list filter and order
            // This can be read as requiring only channels whose "type" is "messaging" AND
            // whose "members" include our "user.id"
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", singletonList(user.getId()))
            );

            ViewModelProvider.Factory factory = new ChannelListViewModelFactory.Builder()
                    .filter(filter)
                    .sort(ChannelListViewModel.DEFAULT_SORT)
                    .build();

            ChannelListViewModel channelsViewModel =
                    new ViewModelProvider(this, factory).get(ChannelListViewModel.class);

            // Step 5 - Connect the ChannelListViewModel to the ChannelListView, loose
            //          coupling makes it easy to customize
            ChannelListViewModelBinding.bind(channelsViewModel, binding.channelListView, getViewLifecycleOwner());
            binding.channelListView.setChannelItemClickListener(
                    channel -> startActivity(ChatActivity.newIntent(requireContext(), channel))
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}