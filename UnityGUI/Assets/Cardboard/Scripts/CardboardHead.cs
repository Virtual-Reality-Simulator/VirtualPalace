﻿// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using UnityEngine;

public class CardboardHead : MonoBehaviour {
  // Which types of tracking this instance will use.
  public bool trackRotation = true;
  public bool trackPosition = true;

  // If set, the head transform will be relative to it.
  public Transform target;

  // Determine whether head updates early or late in frame.
  // Defaults to false in order to reduce latency.
  // Set this to true if you see jitter due to other scripts using this
  // object's orientation (or a child's) in their own LateUpdate() functions,
  // e.g. to cast rays.
  public bool updateEarly = false;
	private Quaternion CurrentRot; 
  // Where is this head looking?
  public Ray Gaze {
    get {
      UpdateHead();
      return new Ray(transform.position, transform.forward);
    }
  }

  	private bool updated;
	public int ViewMoveOn = 0; 

  void Update() {
    updated = false;  // OK to recompute head pose.
    if (updateEarly) {
      UpdateHead();
    }
  }

  // Normally, update head pose now.
  void LateUpdate() {
    UpdateHead();
  }

  // Compute new head pose.
  private void UpdateHead() {
		if (ViewMoveOn > 0) {
			if (updated) {  
				return;
			}
			updated = true;
			//Cardboard.SDK.UpdateState();
			if (trackRotation) {
				//회전 성분 검출(y Rot/360 -> UI Pos X+)

				Quaternion NowRot = Cardboard.SDK.HeadPose.Orientation;
				float YRotVelo = -(CurrentRot.eulerAngles.y - NowRot.eulerAngles.y);
				Vector3 NewVec = Camera.main.transform.right;
				NewVec *= YRotVelo * Time.deltaTime;
				GameObject UI2DMove;
				if (ViewMoveOn == 1)
					UI2DMove = GameObject.Find ("ModelSelectUI");
				else
					UI2DMove = GameObject.Find ("ObjModelSelectUI");
				
				UI2DMove.transform.position += NewVec;

				CurrentRot = NowRot;

			}
			return;
		} else if (ViewMoveOn < 0)
			return;
    if (updated) {  // Only one update per frame, please.
      return;
    }
    updated = true;
    Cardboard.SDK.UpdateState();

    if (trackRotation) {
      var rot = Cardboard.SDK.HeadPose.Orientation;
		CurrentRot = rot;
      if (target == null) {
        transform.localRotation = rot;
      } else {
        transform.rotation = rot * target.rotation;
      }
    }

    if (trackPosition) {
      Vector3 pos = Cardboard.SDK.HeadPose.Position;
      if (target == null) {
        transform.localPosition = pos;
      } else {
        transform.position = target.position + target.rotation * pos;
      }
    }
  }
}
