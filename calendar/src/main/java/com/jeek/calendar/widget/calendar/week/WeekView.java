package com.jeek.calendar.widget.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jeek.calendar.library.R;
import com.jeek.calendar.widget.bean.CalendarBean;
import com.jeek.calendar.widget.calendar.CalendarUtils;
import com.jeek.calendar.widget.calendar.LunarCalendarUtils;
import com.jimmy.common.data.ScheduleDao;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class WeekView extends View {

    private static final int NUM_COLUMNS = 7;
    private Paint mPaint;
    private Paint mLunarPaint;
    private int mNormalDayColor;
    private int mSelectDayColor;
    private int mSelectBGColor;
    private int mSelectBGTodayColor;
    private int mCurrentDayColor;
    private int mHintCircleColor;
    private int mLunarTextColor;
    private int mHolidayTextColor;
    private int mCurrYear, mCurrMonth, mCurrDay;
    private int mSelYear, mSelMonth, mSelDay;
    private int mColumnSize, mRowSize, mSelectCircleSize;
    private int mDaySize;
    private int mLunarTextSize;
    private int mCircleRadius = 6;
    private int[] mHolidays;
    private String mHolidayOrLunarText[];
    private boolean mIsShowLunar;
    private boolean mIsShowHint;
    private boolean mIsShowHolidayHint;
    private DateTime mStartDate;
    private DisplayMetrics mDisplayMetrics;
    private OnWeekClickListener mOnWeekClickListener;
    private GestureDetector mGestureDetector;
    private List<Integer> mTaskHintList;
    private Bitmap mRestBitmap, mWorkBitmap;

    // 日程数据
    private List<CalendarBean> mCalendarBeen;
    private int mScheCircleRadius = 20;// 日程背景圆半径
    private int mScheTxtSize = 28;//日程文字大小

    public WeekView(Context context, DateTime dateTime) {
        this(context, null, dateTime);
    }

    public WeekView(Context context, TypedArray array, DateTime dateTime) {
        this(context, array, null, dateTime);
    }

    public WeekView(Context context, TypedArray array, AttributeSet attrs, DateTime dateTime) {
        this(context, array, attrs, 0, dateTime);
    }

    public WeekView(Context context, TypedArray array, AttributeSet attrs, int defStyleAttr, DateTime dateTime) {
        super(context, attrs, defStyleAttr);
        initAttrs(array, dateTime);
        initPaint();
        initWeek();
        initGestureDetector();
    }

    /**
     * TODO:设置日历标示数据
     *
     * @param calendarBeen
     */
    public void setCalendarList(List<CalendarBean> calendarBeen) {
        mCalendarBeen = calendarBeen;
        invalidate();
    }

    private void initTaskHint(DateTime startDate, DateTime endDate) {
        if (mIsShowHint) {
            ScheduleDao dao = ScheduleDao.getInstance(getContext());
            mTaskHintList = dao.getTaskHintByWeek(startDate.getYear(), startDate.getMonthOfYear() - 1, startDate.getDayOfMonth(), endDate.getYear(), endDate.getMonthOfYear() - 1, endDate.getDayOfMonth());
        }
    }

    private void initAttrs(TypedArray array, DateTime dateTime) {
        if (array != null) {
            mSelectDayColor = array.getColor(R.styleable.WeekCalendarView_week_selected_text_color, Color.parseColor("#FFFFFF"));
            mSelectBGColor = array.getColor(R.styleable.WeekCalendarView_week_selected_circle_color, Color.parseColor("#E8E8E8"));
            mSelectBGTodayColor = array.getColor(R.styleable.WeekCalendarView_week_selected_circle_today_color, Color.parseColor("#FF8594"));
            mNormalDayColor = array.getColor(R.styleable.WeekCalendarView_week_normal_text_color, Color.parseColor("#575471"));
            mCurrentDayColor = array.getColor(R.styleable.WeekCalendarView_week_today_text_color, Color.parseColor("#FF8594"));
            mHintCircleColor = array.getColor(R.styleable.WeekCalendarView_week_hint_circle_color, Color.parseColor("#FE8595"));
            mLunarTextColor = array.getColor(R.styleable.WeekCalendarView_week_lunar_text_color, Color.parseColor("#ACA9BC"));
            mHolidayTextColor = array.getColor(R.styleable.WeekCalendarView_week_holiday_color, Color.parseColor("#A68BFF"));
            mDaySize = array.getInteger(R.styleable.WeekCalendarView_week_day_text_size, 13);
            mLunarTextSize = array.getInteger(R.styleable.WeekCalendarView_week_day_lunar_text_size, 8);
            mIsShowHint = array.getBoolean(R.styleable.WeekCalendarView_week_show_task_hint, true);
            mIsShowLunar = array.getBoolean(R.styleable.WeekCalendarView_week_show_lunar, true);
            mIsShowHolidayHint = array.getBoolean(R.styleable.WeekCalendarView_week_show_holiday_hint, true);
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF");
            mSelectBGColor = Color.parseColor("#E8E8E8");
            mSelectBGTodayColor = Color.parseColor("#FF8594");
            mNormalDayColor = Color.parseColor("#575471");
            mCurrentDayColor = Color.parseColor("#FF8594");
            mHintCircleColor = Color.parseColor("#FE8595");
            mLunarTextColor = Color.parseColor("#ACA9BC");
            mHolidayTextColor = Color.parseColor("#A68BFF");
            mDaySize = 13;
            mDaySize = 8;
            mIsShowHint = true;
            mIsShowLunar = true;
            mIsShowHolidayHint = true;
        }
        mStartDate = dateTime;
        mRestBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rest_day);
        mWorkBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_work_day);
        int holidays[] = CalendarUtils.getInstance(getContext()).getHolidays(mStartDate.getYear(), mStartDate.getMonthOfYear());
        int row = CalendarUtils.getWeekRow(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
        mHolidays = new int[7];
        System.arraycopy(holidays, row * 7, mHolidays, 0, mHolidays.length);
    }


    private void initPaint() {
        mDisplayMetrics = getResources().getDisplayMetrics();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mDaySize * mDisplayMetrics.scaledDensity);

        mLunarPaint = new Paint();
        mLunarPaint.setAntiAlias(true);
        mLunarPaint.setTextSize(mLunarTextSize * mDisplayMetrics.scaledDensity);
        mLunarPaint.setColor(mLunarTextColor);
    }

    private void initWeek() {
        Calendar calendar = Calendar.getInstance();
        mCurrYear = calendar.get(Calendar.YEAR);
        mCurrMonth = calendar.get(Calendar.MONTH);
        mCurrDay = calendar.get(Calendar.DATE);
        DateTime endDate = mStartDate.plusDays(7);
        if (mStartDate.getMillis() <= System.currentTimeMillis() && endDate.getMillis() > System.currentTimeMillis()) {
            if (mStartDate.getMonthOfYear() != endDate.getMonthOfYear()) {
                if (mCurrDay < mStartDate.getDayOfMonth()) {
                    setSelectYearMonth(mStartDate.getYear(), endDate.getMonthOfYear() - 1, mCurrDay);
                } else {
                    setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mCurrDay);
                }
            } else {
                setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mCurrDay);
            }
        } else {
            setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
        }
        initTaskHint(mStartDate, endDate);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                doClickAction((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }

    public void setSelectYearMonth(int year, int month, int day) {
        mSelYear = year;
        mSelMonth = month;
        mSelDay = day;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = mDisplayMetrics.densityDpi * 200;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mDisplayMetrics.densityDpi * 300;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initSize();
        clearData();
        drawThisWeek(canvas);
        drawLunarText(canvas);
        drawHoliday(canvas);
    }

    private void clearData() {
        mHolidayOrLunarText = new String[7];
    }

    private void initSize() {
        mColumnSize = getWidth() / NUM_COLUMNS;
        mRowSize = getHeight();
        mSelectCircleSize = (int) (mColumnSize / 2.8);
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.3);
        }
    }

    private void drawThisWeek(Canvas canvas) {
        for (int i = 0; i < 7; i++) {
            DateTime date = mStartDate.plusDays(i);
            int day = date.getDayOfMonth();
            String dayString = String.valueOf(day);
            int startX = (int) (mColumnSize * i + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            if (day == mSelDay) {
                int startRecX = mColumnSize * i;
                int endRecX = startRecX + mColumnSize;
                if (date.getYear() == mCurrYear && date.getMonthOfYear() - 1 == mCurrMonth && day == mCurrDay) {
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    mPaint.setColor(mSelectBGColor);
                }
                canvas.drawCircle((startRecX + endRecX) / 2, mRowSize / 2, mSelectCircleSize, mPaint);
            }
//            drawHintCircle(i, day, canvas);
            drawCalendar(i, day, canvas);
            if (day == mSelDay) {
                mPaint.setColor(mSelectDayColor);
            } else if (date.getYear() == mCurrYear && date.getMonthOfYear() - 1 == mCurrMonth && day == mCurrDay && day != mSelDay && mCurrYear == mSelYear) {
                mPaint.setColor(mCurrentDayColor);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            mHolidayOrLunarText[i] = CalendarUtils.getHolidayFromSolar(date.getYear(), date.getMonthOfYear() - 1, day);
        }
    }
    /**
     * TODO: 绘制日历标示
     * @param column
     * @param day
     * @param canvas
     */
    private void drawCalendar(int column, int day, Canvas canvas) {
        if (mCalendarBeen == null|| mCalendarBeen.size() == 0)
            return;
        if (mCalendarBeen.get(0).getType() == CalendarBean.KEY_TYPE_NUMBER)//绘制数字
            drawCalendarNumber(column, day, canvas);
        else if (mCalendarBeen.get(0).getType() == CalendarBean.KEY_TYPE_TURE_OR_FALSE)
            drawCalendarState(column, day, canvas);
    }

    /**
     * TODO:绘制日历标示　true | false
     */
    private void drawCalendarState(int column, int day, Canvas canvas) {
        Paint paint = new Paint();
        for (int i = 0; i < mCalendarBeen.size(); i++) {
            if (mCalendarBeen.get(i).getDay() == day &&
                    mCalendarBeen.get(i).getYear() == mSelYear &&
                    mCalendarBeen.get(i).getMonth() == mSelMonth) {
                if (mCalendarBeen.get(i).getState() == CalendarBean.KEY_STATE_NORMAL)
                    return;
                // 右上角位置
                float x1 = (float) (mColumnSize * column + mColumnSize * 0.65);
                float y1 = (float) (mRowSize * 0.15);
                if (mCalendarBeen.get(i).getState() == CalendarBean.KEY_STATE_TRUE)//为true
                {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_true);
                    canvas.drawBitmap(bitmap, x1,y1, paint);
                }
                if (mCalendarBeen.get(i).getState() == CalendarBean.KEY_STATE_FALSE){
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_false);
                    canvas.drawBitmap(bitmap, x1,y1, paint);
                }
            }
        }

    }


    private void drawCalendarNumber(int column, int day, Canvas canvas) {
        Paint paint = new Paint();
        for (int i = 0; i < mCalendarBeen.size(); i++) {
            if (mCalendarBeen.get(i).getDay() == day &&
                    mCalendarBeen.get(i).getYear() == mSelYear &&
                    mCalendarBeen.get(i).getMonth() == mSelMonth) {
                if (mCalendarBeen.get(i).getNumber() <= 0)
                    return;
                paint.setColor(mHintCircleColor);
                // 右上角位置
                float x1 = (float) (mColumnSize * column + mColumnSize * 0.75);
                float y1 = (float) (mRowSize * 0.25);
                canvas.drawCircle(x1, y1, mScheCircleRadius, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(mScheTxtSize);
                float x = (float) (mColumnSize * column + mColumnSize * 0.7);
                float y = (float) (mRowSize * 0.32);
                canvas.drawText(mCalendarBeen.get(i).getNumber() + "", x, y, paint);
            }
        }

    }

    /**
     * 绘制农历
     *
     * @param canvas
     */
    private void drawLunarText(Canvas canvas) {
        if (mIsShowLunar) {
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(mStartDate.getYear(), mStartDate.getMonthOfYear(), mStartDate.getDayOfMonth()));
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            int days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            int day = lunar.lunarDay;
            for (int i = 0; i < 7; i++) {
                if (day > days) {
                    day = 1;
                    if (lunar.lunarMonth == 12) {
                        lunar.lunarMonth = 1;
                        lunar.lunarYear = lunar.lunarYear + 1;
                    }
                    if (lunar.lunarMonth == leapMonth) {
                        days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
                    } else {
                        lunar.lunarMonth++;
                        days = LunarCalendarUtils.daysInLunarMonth(lunar.lunarYear, lunar.lunarMonth);
                    }
                }
                mLunarPaint.setColor(mHolidayTextColor);
                String dayString = mHolidayOrLunarText[i];
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                }
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarDayString(day);
                    mLunarPaint.setColor(mLunarTextColor);
                }
                int startX = (int) (mColumnSize * i + (mColumnSize - mLunarPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * 0.72 - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2);
                canvas.drawText(dayString, startX, startY, mLunarPaint);
                day++;
            }
        }
    }

    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(), mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % 7;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth() - distance, distance, mColumnSize * (column + 1) - distance, mRestBitmap.getHeight() + distance);
                if (mHolidays[i] == 1) {
                    canvas.drawBitmap(mRestBitmap, rect, rectF, null);
                } else if (mHolidays[i] == 2) {
                    canvas.drawBitmap(mWorkBitmap, rect, rectF, null);
                }
            }
        }
    }

    /**
     * 绘制圆点提示
     *
     * @param column
     * @param day
     * @param canvas
     */
    private void drawHintCircle(int column, int day, Canvas canvas) {
        if (mIsShowHint && mTaskHintList != null && mTaskHintList.size() > 0) {
            if (!mTaskHintList.contains(day))
                return;
            mPaint.setColor(mHintCircleColor);
            float circleX = (float) (mColumnSize * column + mColumnSize * 0.5);
            float circleY = (float) (mRowSize * 0.75);
            canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private void doClickAction(int x, int y) {
        if (y > getHeight())
            return;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        DateTime date = mStartDate.plusDays(column);
        clickThisWeek(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
    }

    public void clickThisWeek(int year, int month, int day) {
        if (mOnWeekClickListener != null) {
            mOnWeekClickListener.onClickDate(year, month, day);
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }

    public void setOnWeekClickListener(OnWeekClickListener onWeekClickListener) {
        mOnWeekClickListener = onWeekClickListener;
    }

    /**
     * 获取当前选择年
     *
     * @return
     */
    public int getSelectYear() {
        return mSelYear;
    }

    /**
     * 获取当前选择月
     *
     * @return
     */
    public int getSelectMonth() {
        return mSelMonth;
    }

    /**
     * 获取当前选择日
     *
     * @return
     */
    public int getSelectDay() {
        return this.mSelDay;
    }

    /**
     * 设置圆点提示的集合
     *
     * @param taskHintList
     */
    public void setTaskHintList(List<Integer> taskHintList) {
        mTaskHintList = taskHintList;
        invalidate();
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    public void addTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (!mTaskHintList.contains(day)) {
                mTaskHintList.add(day);
                invalidate();
            }
        }
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    public void removeTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (mTaskHintList.remove(day)) {
                invalidate();
            }
        }
    }
}