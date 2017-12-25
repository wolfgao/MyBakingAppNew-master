package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by gaochuang on 2017/12/15.
 */

public class MyPlayer implements View.OnClickListener, PlaybackControlView.VisibilityListener{

    private View mRootView;
    private Button retryButton;
    private LinearLayout debugRootView;
    private SimpleExoPlayerView simpleExoPlayerView;

    private String mUrl;
    private Context mContext;
    private Intent mIntent;
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    public static final String EXTENSION_EXTRA = "extension";


    private Handler mainHandler;
    private EventLogger eventLogger;
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean inErrorState;
    private TrackGroupArray lastSeenTrackGroupArray;
    private String userAgent;



    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;


    //default constructor
    public MyPlayer(Context context, View rootView) {
        mContext = context;
        mRootView = rootView;
        shouldAutoPlay = true;
        clearResumePosition();
        userAgent = Util.getUserAgent(context, "MyBakingApp");
        mediaDataSourceFactory = buildDataSourceFactory(BANDWIDTH_METER);
        mainHandler = new Handler();

        retryButton = (Button) rootView.findViewById(R.id.retry_button);
        debugRootView = (LinearLayout) rootView.findViewById(R.id.controls_root);
        simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.step_video);

        retryButton.setOnClickListener(this);
        simpleExoPlayerView.setControllerVisibilityListener(this);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            releasePlayer();
        }
    }



    // Internal methods
    public void initializePlayer() {

        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            // 1. Create a default TrackSelector
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            lastSeenTrackGroupArray = null;
            eventLogger = new EventLogger(trackSelector, mContext);


            // 2. Create the player
            boolean preferExtensionDecoders = mIntent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            @DefaultRenderersFactory.ExtensionRendererMode
            int extensionRendererMode =DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mContext,
                    null, extensionRendererMode);

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

            player.addListener(new PlayerEventListener());
            player.addListener(eventLogger);
            player.addMetadataOutput(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);

            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);

        }

        Uri[] uris;
        String[] extensions;
        uris = new Uri[]{mIntent.getData()};
        extensions = new String[]{mIntent.getStringExtra(EXTENSION_EXTRA)};

        MediaSource[] mediaSources = new MediaSource[uris.length];
        for (int i = 0; i < uris.length; i++) {
            mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
        }
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);

        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
        }
        player.prepare(mediaSource, !haveResumePosition, false);
        inErrorState = false;
        updateButtonVisibilities();
    }

    /**
     * @param uri
     * @param overrideExtension
     * @return
     */
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        @ContentType int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(null),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(null),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     *
     */
    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = Math.max(0, player.getContentPosition());
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    // OnClickListener methods
    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            if(mUrl != null || mUrl != "") {
                initializePlayer();
            }
            else{
                Toast.makeText(mContext, "本步骤没有视频", Toast.LENGTH_SHORT).show();
            }

        }
    }

    // PlaybackControlView.VisibilityListener implementation
    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
        retryButton.setVisibility(visibility);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    // User controls

    private void updateButtonVisibilities() {
        debugRootView.removeAllViews();

        retryButton.setVisibility(inErrorState ? View.VISIBLE : View.GONE);
        debugRootView.addView(retryButton);

        if (player == null) {
            return;
        }
    }

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(mContext.getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void setIntent(String url) {
        mIntent = new Intent(mContext, DetailActivity.class);
        mIntent.setData(Uri.parse(url))
                .putExtra(PREFER_EXTENSION_DECODERS, true);
    }

    public void startPlay() {
        if(!inErrorState){
            initializePlayer();
        }
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
            updateButtonVisibilities();
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (inErrorState) {
                // This will only occur if the user has performed a seek whilst in the error state. Update
                // the resume position so that if the user then retries, playback will resume from the
                // position to which they seeked.
                updateResumePosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            String errorString = null;
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    DecoderInitializationException decoderInitializationException =
                            (DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                            errorString = mContext.getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = mContext.getString(R.string.error_no_secure_decoder,
                                    decoderInitializationException.mimeType);
                        } else {
                            errorString = mContext.getString(R.string.error_no_decoder,
                                    decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString = mContext.getString(R.string.error_instantiating_decoder,
                                decoderInitializationException.decoderName);
                    }
                }
            }
            if (errorString != null) {
                showToast(errorString);
            }
            inErrorState = true;
            if (isBehindLiveWindow(e)) {
                clearResumePosition();
                initializePlayer();
            } else {
                updateResumePosition();
                updateButtonVisibilities();
                showControls();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            updateButtonVisibilities();
            if (trackGroups != lastSeenTrackGroupArray) {
                MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }
}
