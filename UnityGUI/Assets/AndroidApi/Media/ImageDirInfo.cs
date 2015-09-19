using System.Collections.Generic;
using UnityEngine;
using LitJson;

namespace AndroidApi.Media
{
	public class ImageDirInfo : BaseDirInfo<ImageInfo>
	{
		protected const string ImageDirInfoClassName = "kr.poturns.virtualpalace.media.image.ImageDirInfo";

		private ImageDirInfo ()
		{
		}

		public static List<ImageDirInfo> GetDirInfoList (AndroidJavaObject activity)
		{
			using (AndroidJavaClass imageBucketInfoClass = new AndroidJavaClass(ImageDirInfoClassName)) {
				string listJson = imageBucketInfoClass.CallStatic<string> (GetJsonDirListMethodName, activity);

				//Debug.Log(listJson);


				JsonData jData = JsonMapper.ToObject (listJson);

				int count = jData.Count;

				List<ImageDirInfo> list = new List<ImageDirInfo> (count);
				for (int i = 0; i < count; i++) {
					ImageDirInfo info = new ImageDirInfo (){
						DirName = (string)jData [i]["dirName"],
						FirstInfo = ImageInfo.parseJSON (jData [i]["firstInfo"])
					};

					list.Add (info);

				}

				return list;
			}
		}

		public override List<ImageInfo> GetInfoList (AndroidJavaObject activity)
		{
			ImageInfo.GetImageInfoList (activity);
		}

	}
}
