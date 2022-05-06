from influxdb_client import InfluxDBClient, Point
import signal
import time  # For the demo only
import argparse
import numpy as np

interrupted = False


def signal_handler(signal, frame):
    global interrupted
    interrupted = True


def parse_argumets():
    parser = argparse.ArgumentParser(description='Data generator for influxDV')
    parser.add_argument('--api-address', dest="api_address", type=str, required=True,
                        help='InfluxDB api address with port')
    parser.add_argument('--token', dest='token', type=str, required=True,
                        help='InfluxDB token with write access')
    parser.add_argument('--bucket', dest='bucket', type=str, required=True,
                        help='InfluxDB bucket to write')
    parser.add_argument('--organization', dest='org', type=str, required=True,
                        help='InfluxDB bucket to write')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_argumets()

    signal.signal(signal.SIGINT, signal_handler)

    client = InfluxDBClient(url=args.api_address, token=args.token, verify_ssl=False)
    if not client.ping():
        print("Unable to connect to InfluxDB")
        exit(1)
    write_api = client.write_api()

    timeArray = np.arange(0, 10, 0.1)
    amplitude = np.sin(timeArray)
    amplitude_size = len(amplitude)
    current_index = 0

    while True:
        if current_index == amplitude_size:
            current_index = 0
        print("Values uploaded to influx! Temperature: %5.3f Weight: %3d"
              % (amplitude[current_index] * 10, abs(amplitude[current_index] * 10)))
        write_api.write(args.bucket, args.org,
                        Point("bee_weight")
                        .tag("location", "place_1")
                        .field("temperature", amplitude[current_index] * 10)
                        .field("weight", abs(amplitude[current_index] * 10)))
        time.sleep(1)
        current_index += 1
        if interrupted:
            print("Signal received, stopping data generator")
            break
