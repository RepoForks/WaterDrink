package com.skydoves.waterdays.ui.fragments.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.skydoves.waterdays.R
import com.skydoves.waterdays.WDApplication
import com.skydoves.waterdays.events.rx.RxUpdateMainEvent
import com.skydoves.waterdays.models.Alarm
import com.skydoves.waterdays.persistence.sqlite.SqliteManager
import com.skydoves.waterdays.ui.activities.main.MakeAlarmActivity
import com.skydoves.waterdays.ui.adapters.AlarmFragmentAdapter
import com.skydoves.waterdays.ui.viewholders.AlarmViewHolder
import com.skydoves.waterdays.utils.AlarmUtils
import com.skydoves.waterdays.utils.DateUtils
import kotlinx.android.synthetic.main.layout_setnotification.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject

/**
 * Created by skydoves on 2016-10-15.
 * Updated by skydoves on 2017-08-20.
 * Copyright (c) 2017 skydoves rights reserved.
 */

class AlarmFragment : Fragment() {

    @Inject lateinit var sqliteManager: SqliteManager
    @Inject lateinit var alarmUtils: AlarmUtils

    private var rootView: View? = null
    private var adapter: AlarmFragmentAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.layout_setnotification, container, false)
        WDApplication.component.inject(this)
        this.rootView = rootView
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        InitializeUI()
    }

    private fun InitializeUI() {
        adapter = AlarmFragmentAdapter(delegate)
        recyclerView!!.adapter = adapter

        val cursor = sqliteManager.readableDatabase.rawQuery("select * from AlarmList", null)
        try {
            if (cursor != null && cursor.count > 0 && cursor.moveToLast()) {
                do {
                    val requestCode = cursor.getInt(0)
                    val days = cursor.getString(1)
                    val startTime = cursor.getString(2)
                    val endTime = cursor.getString(3)
                    val interval = cursor.getString(4)
                    val sday = DateUtils.getDayNameList(days)
                    val alarmModel = Alarm(requestCode, sday, startTime + " ~ " + endTime, "간격 : " + interval + "시간")
                    adapter!!.addAlarmItem(alarmModel)
                } while (cursor.moveToPrevious())
            } else
                rootView!!.findViewById(R.id.setnotification_tv_message).visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor!!.close()
        }

        setnotification_fab.setOnClickListener {
            startActivity<MakeAlarmActivity>()
        }
    }

    val delegate = object : AlarmViewHolder.Delegate {
        override fun onConfirm(alarmModel: Alarm) {
            try {
                alarmUtils.cancelAlarm(alarmModel.requestCode)
                sqliteManager.deleteAlarm(alarmModel.requestCode)
                adapter!!.sections()[0].remove(alarmModel)

                Toast.makeText(context, "알람이 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                RxUpdateMainEvent.getInstance().sendEvent()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}