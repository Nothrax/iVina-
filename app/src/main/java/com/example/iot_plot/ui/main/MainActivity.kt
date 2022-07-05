package com.example.iot_plot.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import com.example.iot_plot.R
import com.example.iot_plot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var graphSpinner: Spinner
    private lateinit var chooserView: LinearLayout
    private lateinit var addView: LinearLayout

    enum class ViewSwitch{
        CHOOSER,
        CREATOR

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val createPlotButton = binding.createPlotButton
        val deletePlotButton = binding.deletePlotButton
        val savePlotButton = binding.saveGraphButton
        graphSpinner = binding.graphChooser
        addView = binding.graphCreatorView
        chooserView = binding.grapChooserView

        switchView(ViewSwitch.CHOOSER)

        createPlotButton.setOnClickListener{
            switchView(ViewSwitch.CREATOR)
        }

        savePlotButton.setOnClickListener{
            //todo
            switchView(ViewSwitch.CHOOSER)
        }


    }

    fun switchView(viewSwitch: ViewSwitch){
        when(viewSwitch){
            ViewSwitch.CHOOSER ->{
                addView.visibility = View.GONE
                chooserView.visibility = View.VISIBLE
            }
            ViewSwitch.CREATOR ->{
                addView.visibility = View.VISIBLE
                chooserView.visibility = View.GONE
            }
        }
    }
}