using UnityEngine;
using MyScript.Interface;
using BridgeApi.Controller;
using System.Collections;
using System.Collections.Generic;

namespace MyScript.States
{

    public class ARSceneIdleState : AbstractGazeInputState, ISceneChangeState
    {
        private GameObject ARScreenObj;
		private List<ARRenderingObject> ObjList;
		private GameObject ARObjPrefab;
        public ARSceneIdleState(StateManager managerRef) : base(managerRef, "ARSceneIdleState")
        {

        }

        public UnityScene UnitySceneID { get { return UnityScene.AR; } }

        public void OnSceneChanged()
        {
            Debug.Log("=============== " + Name + " : Scene changed");
            Init();
        }


        protected override void Init()
        {
            base.Init();
            // 스크린의 게임오브젝트를 가져온다
            if (ARScreenObj == null)
                ARScreenObj = GameObject.Find("ARView");

			ObjList = new List<ARRenderingObject> ();
			ARObjPrefab = GameObject.Find ("PrefabStore").GetComponent<ARPrefabload> ().ARPrefab;
			if(ARObjPrefab) Debug.Log ("AR Prefab is Null");

        }

        protected override void HandleCancelOperation()
        {
            ReturnToLobbyScene();
        }

        private void ReturnToLobbyScene()
        {
            StateManager.SwitchScene(UnityScene.Lobby);
        }

        protected override void HandleOtherOperation(Operation operation)
        {
            //base.HandleOtherOperation(operation);
            switch (operation.Type)
            {
                case Operation.AR_RENDERING:
                    ARrenderItem item = JsonInterpreter.ParseARrenderItem(operation);
                    Debug.Log("====== AR item : " + item);
					//Update 리턴값이 false 면 생성
					if(!ARItemUpdate(item))
					{
						GameObject NewObj = GameObject.Instantiate(ARObjPrefab) as GameObject;
						ARRenderingObject NewAR = ARObjPrefab.GetComponent<ARRenderingObject>();
						ObjList.Add(NewAR);
					}
                    break;

                default:
                    return;
            }

        }
		//item을 받아서 List를 검사한뒤 있으면 업데이트하고 ture반환
		//없으면 false 반환
		private bool ARItemUpdate(ARrenderItem item)
		{
			if (ObjList == null || ObjList.Count == 0) 
				return false;

			foreach (ARRenderingObject obj in ObjList) 
			{
				if(obj.ARItem.resId == item.resId)
				{
					obj.ARItem.screenX = item.screenX;
					obj.ARItem.screenY= item.screenY;
					obj.SetARPosition(item);
					return true;
				}
			}

			return false;
		}
    }
}

