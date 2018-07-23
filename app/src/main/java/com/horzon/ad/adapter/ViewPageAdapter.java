package com.horzon.ad.adapter;

import java.util.List;

import com.horzon.ad.video.R;
import com.horzon.ad.tool.ImgInfo;
import com.horzon.utils.LogUtil;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ViewPageAdapter extends PagerAdapter{

    private static final String TAG = "ViewPageAdapter" ;

	private List<ImgInfo>  imageViewList;
	private Context mContext;
	private LayoutInflater inflater;



	public ViewPageAdapter(Context  context , List<ImgInfo> list){
		this.imageViewList = list;
		this.mContext = context;
		inflater = LayoutInflater.from(context);
	}

	public void addImgInfo(ImgInfo info){
		imageViewList.add(info);
		notifyDataSetChanged();


        LogUtil.d(TAG,"SIZE:"+imageViewList.size());
	}

	public void removeImgInfo(ImgInfo info) {
		imageViewList.remove(info);
		notifyDataSetChanged();

        LogUtil.d(TAG,"SIZE:"+imageViewList.size());
	}



	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	/**
	 * 判断出去的view是否等于进来的view 如果为true直接复用
	 */
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	/**
	 * 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
//		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.a2);
//		container.startAnimation(animation);
		//container.removeView(imageViewList.get(position % imageViewList.size()));
		container.removeView((View)object);
	}

	/**
	 * 创建一个view
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		View view = View.inflate(mContext, R.layout.adapter_img, null);


		if(imageViewList.size() > 0) {

			ImageView imageView = (ImageView) view.findViewById(R.id.vd_img);
			ImgInfo ad = imageViewList.get(position % imageViewList.size());

			if(ad.getIconResUri() == null) {
				imageView.setImageResource(ad.getIconResId());
			}
			else{
				imageView.setImageURI(ad.getIconResUri());
			}


			container.addView(view);    // 一定不能少，将view加入到viewPager中
		}

		return view;

	}





}
