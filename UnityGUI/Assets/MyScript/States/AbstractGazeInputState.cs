﻿using MyScript;
using MyScript.Interface;
using UnityEngine;

namespace MyScript.States
{
    /// <summary>
    /// EventSystem과 GazeInputModule을 가진 StateBase 클래스
    /// </summary>
    public abstract class AbstractGazeInputState : AbstractInputHandleState
    {
        private GameObject eventSystem;
        private GazeInputModule selectModule;
        private GazeCusor gazecusor;
        private CardboardHead cameraHead;

        public GameObject EventSystem { get { return eventSystem; } }
        public GazeInputModule SelectModule { get { return selectModule; } }
        public GazeCusor GCursor { get { return gazecusor; } }
        

        public AbstractGazeInputState(StateManager managerRef, string stateName) : base(managerRef, stateName)
        {
            Init();
        }

        public AbstractGazeInputState(AbstractGazeInputState otherStateInSameScene, string stateName) : this(otherStateInSameScene.Manager, stateName)
        {
            eventSystem = otherStateInSameScene.eventSystem;
            selectModule = otherStateInSameScene.selectModule;
            gazecusor = otherStateInSameScene.gazecusor;
            cameraHead = otherStateInSameScene.cameraHead;
        }

        /// <summary>
        /// EventSystem과 SelectModule을 초기화한다.
        /// </summary>
        protected virtual void Init()
        {
            FindEventSystemAndSelectModule();
        }

        /// <summary>
        /// GazeCursor의 SelectMode를 변경한다.
        /// </summary>
        /// <param name="selectMode">변경할 모드</param>
        protected void SetGazeInputMode(GAZE_MODE selectMode)
        {
            gazecusor.Mode = selectMode;
        }


        /// <summary>
        /// CameraHead를  고정한다.
        ///  </summary>
        protected void SetCameraLock(bool LockOn)
        {
            //cameraHead.ViewMoveOn = LockOn ? -1 : 0;
            if (LockOn)
                cameraHead.ViewMoveOn = -1;
            else
                cameraHead.ViewMoveOn = 0;
        }

        /// <summary>
        /// EventSystem과 SelectModule을 찾아서 할당한다.
        /// </summary>
        protected virtual void FindEventSystemAndSelectModule()
        {

            if (selectModule == null)
            {
                if (eventSystem == null)
                {
                    eventSystem = GameObject.Find("EventSystem");
                }
                if (eventSystem != null)
                {
                    selectModule = eventSystem.GetComponent<GazeInputModule>();
                    if (selectModule == null)
                    {
                        Debug.LogError("Cannot find GazeInputModule");
                    }
                    gazecusor = selectModule.cursor.GetComponent<GazeCusor>();
                    if (gazecusor == null)
                    {
                        Debug.LogError("Cannot find GazeCursor");
                    }
                }
                else
                    Debug.LogError("Cannot find EventSystem");
            }

            if (cameraHead == null)
                cameraHead = GameObject.Find("Head").GetComponent<CardboardHead>();

        }

        protected override void HandleSelectOperation()
        {
            FindEventSystemAndSelectModule();

            GameObject SelObj = selectModule.RaycastedGameObject;
            if (SelObj != null)
            {
                IRaycastedObject raycastedObject = SelObj.GetComponent<IRaycastedObject>();
                if (raycastedObject != null)
                {
                    raycastedObject.OnSelect();
                }
                else
                {
                    Debug.Log("raycastedObject == null");
                }
            }
            else
            {
                Debug.Log("Select Object == null");
            }
        }

		//Toast Message Test Code
		protected override void HandleDeepOperation()
		{
			Debug.Log ("Catch Deep");
			MessegeToaster toaster = GameObject.Find ("MessageToaster").GetComponent<MessegeToaster>();

			if (toaster == null)
				Debug.Log ("!!!!!!!!!!!!!!!!!!!Toaster is Null!!!!!!!!!!!!!!!!!!!!!");
			else 
			{
				toaster.CallMessageToaster("");
			}
		}


    }
}
