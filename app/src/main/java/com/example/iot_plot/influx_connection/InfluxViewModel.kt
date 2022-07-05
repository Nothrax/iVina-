package com.example.iot_plot.influx_connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.iot_plot.R
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InfluxViewModel(private val influxRepository: InfluxRepository) : ViewModel() {
    private val _influxForm = MutableLiveData<InfluxFormState>()
    val influxFormState: LiveData<InfluxFormState> = _influxForm

    private val _influxResult = MutableLiveData<InfluxResult>()
    val influxResult: LiveData<InfluxResult> = _influxResult

    private val executor: ExecutorService = Executors.newCachedThreadPool()

    fun login(serverAddress: String, organization: String, token: String) {
        executor.execute{
            try{
                val result = influxRepository.makePingRequest(serverAddress, token, organization)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(apiAddress = serverAddress, token = token, organization = organization)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.login_failed))
            }
        }
    }

    fun getBuckets(serverAddress: String, organization: String, token: String){
        executor.execute{
            try{
                val result = influxRepository.makeBucketListRequest(serverAddress, token, organization)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(bucketList = result)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.login_failed))
            }
        }
    }

    fun getLocation(serverAddress: String, organization: String, token: String, bucket: String){
        executor.execute{
            try{
                val result = influxRepository.makeLocationListRequest(serverAddress, token, organization, bucket)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(locationList = result)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.login_failed))
            }
        }
    }

    fun getDevice(serverAddress: String, organization: String, token: String, bucket: String, location: String){
        executor.execute{
            try{
                val result = influxRepository.makeDeviceListRequest(serverAddress, token, organization, bucket, location)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(deviceList = result)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.login_failed))
            }
        }
    }

    fun getMeasurements(serverAddress: String, organization: String, token: String, bucket: String, location: String, device: String){
        executor.execute{
            try{
                val result = influxRepository.makeMeasurementListRequest(serverAddress, token, organization, bucket, location, device)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(measurementList = result)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.login_failed))
            }
        }
    }

    fun getGraphData(serverAddress: String, organization: String, token: String, bucket: String, location: String, device: String,
                     measurements: List<String>, timeFrom: Calendar, timeTo: Calendar
    ){
        executor.execute{
            try{
                val result = influxRepository.makeGraphRequest(serverAddress, token, organization, bucket, location, device, measurements, timeFrom, timeTo)
                _influxResult.postValue(InfluxResult(success = InfluxDataView(dataList = result)))
            }catch (e: java.lang.Exception){
                _influxResult.postValue(InfluxResult(error = R.string.data_load_failed))
            }
        }
    }

    fun loginDataChanged(serverAddress: String, organization: String, token: String) {
        if (!isAddressValid(serverAddress)) {
            _influxForm.value = InfluxFormState(addressError = R.string.invalid_address)
        } else if (!isOrganizationValid(organization)) {
            _influxForm.value = InfluxFormState(organizationError = R.string.invalid_organization)
        } else if(!isTokenValid(token)){
            _influxForm.value = InfluxFormState(tokenError = R.string.invalid_token)
        }else{
            _influxForm.value = InfluxFormState(isDataValid = true)
        }
    }

    private fun isAddressValid(address: String): Boolean {
        //todo better check?
        return address.isNotBlank()
    }

    private fun isOrganizationValid(organization: String): Boolean {
        //todo better check?
        return organization.isNotBlank()
    }

    private fun isTokenValid(token: String): Boolean {
        //todo better check?
        return token.length > 5
    }
}