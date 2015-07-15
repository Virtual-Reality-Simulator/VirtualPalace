package kr.poturns.util;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class SpeechToTextHelper extends InputHandleHelper.ContextInputHandleHelper {
    static final String TAG = "SpeechToTextHelper";

    public interface STTListener {
        void onReadyForSpeech();

        void onBeginningOfSpeech();

        void onEndOfSpeech();

        void onError(int error);

        void onBufferReceived(byte[] buffer);

        void onResults(boolean isPartial, String[] resultList, float[] confidences);

    }


    private SpeechRecognizer mRecognizer;
    private Intent speechIntent;
    boolean isInVoiceRecognition = false;
    STTListener mListener;

    public SpeechToTextHelper(Context context, STTListener listener) {
        super(context);
        this.mListener = listener;
        initSTT();
    }

    public SpeechToTextHelper(Context context) {
        super(context);
        initSTT();
    }

    public void setListener(STTListener listener) {
        this.mListener = listener;
    }

    private void initSTT() {
        //음성인식 intent생성
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //데이터 설정
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        //음성인식 언어 설정
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        speechIntent.putExtra("android.speech.extra.GET_AUDIO", true);

        //음성인식 객체
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        //음성인식 리스너 등록
        mRecognizer.setRecognitionListener(recognitionListener);
    }

    public static String[] getRecognitionResult(Bundle result) {
        return (String[]) result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).toArray();
    }

    public static float[] getConfidenceResult(Bundle result) {
        return result.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
    }


    @Override
    public void resume() {
        //f.startActivityForResult(speechIntent, 1010);
    }

    @Override
    public void start() {
        mRecognizer.startListening(speechIntent);
    }

    @Override
    public void pause() {
        mRecognizer.stopListening();
    }

    public void cancel() {
        mRecognizer.cancel();
    }

    @Override
    public void destroy() {
        mRecognizer.destroy();
    }

    public boolean isInVoiceRecognition() {
        return isInVoiceRecognition;
    }

    private final RecognitionListener recognitionListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
            mListener.onReadyForSpeech();
        }

        @Override
        public void onBeginningOfSpeech() {
            isInVoiceRecognition = true;
            mListener.onBeginningOfSpeech();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            mListener.onBufferReceived(buffer);
        }

        @Override
        public void onEndOfSpeech() {
            isInVoiceRecognition = false;
            mListener.onEndOfSpeech();
        }

        @Override
        public void onError(int error) {
            isInVoiceRecognition = false;
            mListener.onError(error);
        }

        @Override
        public void onResults(Bundle results) {
            mListener.onResults(false, getRecognitionResult(results), getConfidenceResult(results));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            mListener.onResults(true, getRecognitionResult(partialResults), getConfidenceResult(partialResults));
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            //mListener.onEvent(eventType, params);
        }
    };
}
