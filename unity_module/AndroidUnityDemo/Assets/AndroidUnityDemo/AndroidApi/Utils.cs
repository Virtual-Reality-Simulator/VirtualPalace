﻿using System;
using UnityEngine;

namespace AndroidApi
{
	public class Utils
	{
		public const string UnityPlayerClassName = "com.unity3d.player.UnityPlayer";

		public const string SpeechToTextListenerClassName = "kr.poturns.util.SpeechToTextHelper$STTListener";
		public const string MessageListenerClassName = "kr.poturns.util.WearableCommHelper$MessageListener";

		public const string RunOnUiThreadMethodName = "runOnUiThread";

		public static AndroidJavaObject GetActivityObject()
		{
			AndroidJavaClass playerClass = new AndroidJavaClass(UnityPlayerClassName);
			AndroidJavaObject activity = playerClass.GetStatic<AndroidJavaObject>("currentActivity");

			playerClass.Dispose ();

			return activity;
		}

		public static InputHandleHelperProxy GetInputHandleHelperProxy(){
			return new InputHandleHelperProxy (GetActivityObject());
		}
	}
}