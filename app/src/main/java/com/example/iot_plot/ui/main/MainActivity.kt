package com.example.iot_plot.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.iot_plot.R
import com.example.iot_plot.databinding.ActivityMainBinding
import com.example.iot_plot.influx_connection.InfluxDataView
import com.example.iot_plot.data.model.InfluxGraphs
import com.example.iot_plot.influx_connection.InfluxViewModel
import com.example.iot_plot.influx_connection.InfluxViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import com.example.iot_plot.data.model.GraphInfo
import com.google.gson.Gson
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var graphSpinner: Spinner
    private lateinit var chooserView: LinearLayout
    private lateinit var addView: LinearLayout
    private lateinit var graphView: LinearLayout

    private lateinit var server: String
    private lateinit var token: String
    private lateinit var organization: String

    private lateinit var influxViewModel: InfluxViewModel
    private lateinit var influxGraphs: InfluxGraphs

    private val fromCalendar = Calendar.getInstance()
    private val toCalendar = Calendar.getInstance()

    enum class ViewSwitch {
        CHOOSER,
        CREATOR,
        PLOT

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val createPlotButton = binding.createPlotButton
        val deletePlotButton = binding.deletePlotButton
        val savePlotButton = binding.saveGraphButton
        val showPlotButton = binding.showPlotButton
        val retButton = binding.graphCloseButton
        graphSpinner = binding.graphChooser
        addView = binding.graphCreatorView
        chooserView = binding.grapChooserView
        graphView = binding.graphLayout

        val b = getIntent().getExtras()
        if (b != null) {
            server = b.getString("address").toString()
            token = b.getString("token").toString()
            organization = b.getString("organization").toString()
        }

        influxViewModel =
            ViewModelProvider(this, InfluxViewModelFactory()).get(InfluxViewModel::class.java)

        influxViewModel.influxFormState.observe(this@MainActivity, Observer {
            val influxState = it ?: return@Observer

            if (!influxState.isDataValid) {
                Toast.makeText(applicationContext, "Received invalid data!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        influxViewModel.influxResult.observe(this@MainActivity, Observer {
            val queryResult = it ?: return@Observer

            if (queryResult.error != null) {
                Toast.makeText(
                    applicationContext,
                    "Databázový dotaz se nepovedl!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (queryResult.success != null) {
                updateGraphCreator(queryResult.success)
            }
        })

        switchView(ViewSwitch.CHOOSER)

        createPlotButton.setOnClickListener {
            switchView(ViewSwitch.CREATOR)
            resetGraphAdder()
            influxViewModel.getBuckets(server, organization, token)
        }

        savePlotButton.setOnClickListener {
            switchView(ViewSwitch.CHOOSER)
            saveGraph();
        }

        showPlotButton.setOnClickListener {
            plotGraph()
            switchView(ViewSwitch.PLOT)
        }

        deletePlotButton.setOnClickListener {
            deleteGraph()
        }

        retButton.setOnClickListener{
            switchView(ViewSwitch.CHOOSER)
        }

        setUpGraphAdder()
        setupTimePickers()
        loadSavedGraphs()

    }

    private fun plotGraph() {
        val actualGraph = binding.graphChooser.selectedItem.toString()
        for (graphInfo in influxGraphs.graphs) {
            if (graphInfo.graphName == actualGraph) {
                influxViewModel.getGraphData(
                    serverAddress = server,
                    organization = organization,
                    token = token,
                    bucket = graphInfo.bucket,
                    location = graphInfo.place,
                    device = graphInfo.device,
                    measurements = graphInfo.measurements,
                    timeFrom = graphInfo.timeFrom,
                    timeTo = graphInfo.timeTo
                )
                return
            }
        }
    }


    private fun loadSavedGraphs() {
        val mPrefs = getPreferences(MODE_PRIVATE)
        val gson = Gson()
        val json = mPrefs.getString("SavedGraphs", "")
        if (json != "") {
            influxGraphs = gson.fromJson(json, InfluxGraphs::class.java)
        } else {
            influxGraphs = InfluxGraphs(graphs = ArrayList())
        }
        updateSavedGraphsDropdown()
    }

    private fun saveGraph() {
        val bucket = binding.bucketSpinner.selectedItem.toString()
        val place = binding.locationSpinner.selectedItem.toString()
        val device = binding.deviceSpinner.selectedItem.toString()
        val measurements = binding.checkboxLayout
        val childCount = measurements.getChildCount()
        val measurementList: ArrayList<String> = ArrayList()
        val graphName = binding.graphName.text.toString()

        for (i in 0 until childCount) {
            val v: View = measurements.getChildAt(i)
            if (v is CheckBox) {
                if (v.isChecked) {
                    measurementList.add(v.getHint().toString())
                }
            }
        }

        if (bucket == "" || place == "" || device == " " || measurementList.isEmpty() || graphName == "") {

            Toast.makeText(
                applicationContext,
                "Nebyly vyplněny všechny parametry!",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }


        val graphInfo = GraphInfo(
            bucket = bucket,
            place = place,
            device = device,
            measurements = measurementList,
            timeFrom = fromCalendar,
            timeTo = toCalendar,
            graphName = graphName
        )

        for (graph in influxGraphs.graphs) {
            if (graph.graphName == graphInfo.graphName) {
                Toast.makeText(
                    applicationContext,
                    "Graf se stejným názvem již existuje a nebyl vytvořen nový!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }
        }


        influxGraphs.graphs.add(graphInfo)

        updateSavedGraphsDropdown()
    }

    private fun updateSavedGraphsDropdown() {
        val graphDropdown = binding.graphChooser

        val graphs = ArrayList<String>()
        for (graph in influxGraphs.graphs) {
            graphs.add(graph.graphName)
        }
        setValuesInSpinner(graphs, graphDropdown, "Žádné uložené grafy")

        val mPrefs = getPreferences(MODE_PRIVATE)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(influxGraphs)
        prefsEditor.putString("SavedGraphs", json)
        prefsEditor.apply()
    }

    private fun deleteGraph() {
        val actualGraph = binding.graphChooser.selectedItem.toString()
        for (graph in influxGraphs.graphs) {
            if (graph.graphName == actualGraph) {
                influxGraphs.graphs.remove(graph)
                break
            }
        }

        updateSavedGraphsDropdown()
    }

    private fun resetGraphAdder() {
        val bucketSpinner = binding.bucketSpinner
        val locationSpinner = binding.locationSpinner
        val deviceSpinner = binding.deviceSpinner
        val name = binding.graphName

        name.setText("")

        setValuesInSpinner(ArrayList(), bucketSpinner, "Nebyl nalezen žádný účet!")
        setValuesInSpinner(ArrayList(), locationSpinner, "Nebylo nalezeno žádné místo!")
        setValuesInSpinner(ArrayList(), deviceSpinner, "Nebylo nalezeno žádné zařízení!")

    }

    private fun setUpGraphAdder() {
        val bucketSpinner = binding.bucketSpinner

        bucketSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val bucket = bucketSpinner.selectedItem.toString()
                influxViewModel.getLocation(server, organization, token, bucket)
            }

        }

        val locationSpinner = binding.locationSpinner
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val location = locationSpinner.selectedItem.toString()
                val bucket = bucketSpinner.selectedItem.toString()
                influxViewModel.getDevice(server, organization, token, bucket, location)
            }

        }

        val deviceSpinner = binding.deviceSpinner
        binding.deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val bucket = bucketSpinner.selectedItem.toString()
                val location = locationSpinner.selectedItem.toString()
                val device = deviceSpinner.selectedItem.toString()
                influxViewModel.getMeasurements(
                    server,
                    organization,
                    token,
                    bucket,
                    location,
                    device
                )
            }
        }
    }

    private fun updateGraphCreator(data: InfluxDataView) {
        if (data.bucketList != null) {
            val bucketSpinner = binding.bucketSpinner
            setValuesInSpinner(data.bucketList, bucketSpinner, "Nebyl nalezen žádný účet!")
        }
        if (data.locationList != null) {
            val locationSpinner = binding.locationSpinner
            setValuesInSpinner(data.locationList, locationSpinner, "Nebylo nalezeno žádné místo")
        }
        if (data.deviceList != null) {
            val deviceSpinner = binding.deviceSpinner
            setValuesInSpinner(data.deviceList, deviceSpinner, "Nebylo nalezeno žádné zařízení")
        }
        if (data.measurementList != null) {
            val measurementLayout = findViewById<LinearLayout>(R.id.checkbox_layout)

            measurementLayout.removeAllViews()

            for (measurement in data.measurementList) {
                val checkBox = CheckBox(this)
                checkBox.setHint(measurement)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                params.gravity = Gravity.CENTER
                checkBox.layoutParams = params
                checkBox.setTextColor(Color.BLACK)
                checkBox.setHintTextColor(Color.BLACK)
                measurementLayout.addView(checkBox)
            }
            //todo loading screen?
        }
        if (data.dataList != null) {
            //todo move
            if (data.dataList.isEmpty()) return

            val graph = findViewById<View>(R.id.graph) as GraphView
            val rnd = Random()
            graph.removeAllSeries()

            val actualGraph = binding.graphChooser.selectedItem.toString()
            var measurementList = ArrayList<String>()
            for (graphInfo in influxGraphs.graphs) {
                if (graphInfo.graphName == actualGraph) {
                    measurementList = graphInfo.measurements
                }
            }


            for (measurement in measurementList) {
                val series: LineGraphSeries<DataPoint> = LineGraphSeries()
                for (value in data.dataList) {
                    if (value.measurement == measurement) {
                        series.appendData(DataPoint(Date.from(value.time), value.value), true, 1000)
                        series.color =
                            Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                        series.isDrawDataPoints = true
                        series.title = measurement
                    }
                }
                graph.addSeries(series)
            }
            val timeFormat = SimpleDateFormat("HH:mm");
            graph.getGridLabelRenderer()
                .setLabelFormatter(DateAsXAxisLabelFormatter(graph.getContext(), timeFormat));
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP)
        }
    }

    private fun switchView(viewSwitch: ViewSwitch) {
        when (viewSwitch) {
            ViewSwitch.CHOOSER -> {
                chooserView.visibility = View.VISIBLE
                addView.visibility = View.GONE
                graphView.visibility = View.GONE
            }
            ViewSwitch.CREATOR -> {
                addView.visibility = View.VISIBLE
                chooserView.visibility = View.GONE
                graphView.visibility = View.GONE
            }
            ViewSwitch.PLOT -> {
                graphView.visibility = View.VISIBLE
                addView.visibility = View.GONE
                chooserView.visibility = View.GONE
            }
        }
    }

    private fun setValuesInSpinner(list: ArrayList<String>, spinner: Spinner, emptyText: String) {
        if (list.isEmpty()) {
            list.add(emptyText)
            spinner.setEnabled(false)
        } else {
            spinner.setEnabled(true)
        }

        val arrayAdapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    private fun setupTimePickers() {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy");
        val timeFormat = SimpleDateFormat("HH:mm");
        fromCalendar.add(Calendar.DAY_OF_MONTH, -1)

        val fromDate = findViewById<EditText>(R.id.date_from_edit_text)
        fromDate.transformIntoDatePicker(this, "dd/MM/yyyy", fromCalendar)
        fromDate.setText(dateFormat.format(fromCalendar.getTime()))

        val toDate = findViewById<EditText>(R.id.date_to_edit_text)
        toDate.transformIntoDatePicker(this, "dd/MM/yyyy", toCalendar)
        toDate.setText(dateFormat.format(toCalendar.getTime()))

        val fromTime = findViewById<EditText>(R.id.time_from_edit_text)
        fromTime.transformIntoTimePicker(this, "HH:mm", fromCalendar)
        fromTime.setText(timeFormat.format(fromCalendar.getTime()))

        val toTime = findViewById<EditText>(R.id.time_to_edit_text)
        toTime.transformIntoTimePicker(this, "HH:mm", toCalendar)
        toTime.setText(timeFormat.format(toCalendar.getTime()))
    }

    fun EditText.transformIntoDatePicker(
        context: Context,
        format: String,
        calendar: Calendar,
        maxDate: Date? = null
    ) {
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false


        val datePickerOnDataSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(format, Locale.UK)
                setText(sdf.format(calendar.time))
            }


        setOnClickListener {
            DatePickerDialog(
                context, datePickerOnDataSetListener, calendar
                    .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).run {
                maxDate?.time?.also { datePicker.maxDate = it }
                show()
            }
        }
    }

    fun EditText.transformIntoTimePicker(
        context: Context,
        format: String,
        calendar: Calendar,
        maxDate: Date? = null
    ) {
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false


        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            setText(SimpleDateFormat(format).format(calendar.time))
        }

        setOnClickListener {
            TimePickerDialog(
                context,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

    }
}