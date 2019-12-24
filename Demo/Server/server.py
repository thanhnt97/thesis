from flask import request, render_template
from flask import jsonify
from flask_api import FlaskAPI, status
import numpy as np
from numpy import loadtxt
from keras.models import load_model
import librosa
from python_speech_features import logfbank, mfcc
from scipy.io import wavfile
import time
from datetime import date
from flask_cors import CORS
import time
from essentia.standard import *
import soundfile as sf
from scipy.signal import butter, lfilter, filtfilt
from flask_socketio import SocketIO
import json
from engineio.payload import Payload
from flask_socketio import send, emit

from sklearn.utils import shuffle
import os

#Start Declare Global Variables
tomcat_path = '/opt/tomcat/apache-tomcat-8.5.50/webapps/examples/'
tomcat_url = "http://192.168.1.11:8080/examples/"
base_dir = "/media/thanhptit/New Volume/Thesis/data_drive/"
folders = ["normal_1", "normal_2", "normal_3", "wheeze_1/w", "wheeze_2/wheeze", "wheeze_3", "wheeze_4"]
label_of_folders = [1, 1, 1, 0, 0, 0, 0]
data_raw = []
data_raw_labels = []
for i in range(len(folders)):
    for j in os.listdir(base_dir + folders[i]):
        data_raw.append(base_dir + folders[i] + "/" + j)
        data_raw_labels.append(label_of_folders[i])

list_data_client = []
noise_files = [
    "None",
    "fan_noise",
    ""
]



#End Declare Global Variables









dem = np.zeros(48000).astype(np.float32)
real_dem = 0
Payload.max_decode_packets = 500
def butter_highpass(lowcut, fs, order=5):
    nyq = 0.5 * fs
    low = lowcut / nyq
    b, a = butter(order, low , btype='high', analog = False)
    return b, a
def butter_highpass_filter(data, lowcut, fs, order=5):
    b, a = butter_highpass(lowcut, fs, order=order)
    y = filtfilt(b, a, data)
    return y
linktofile = []
labeloffile = []
preframe0 = [0] * 24000
preframe1 = [0] * 16000
preframe2 = [0] * 8000
preframe3 = [0] * 8000
preframe4 = [0] * 8000
number = 0
mean = np.load("mean.npy")
std = np.load("std.npy")
mean1 = np.load("mean_final_1.npy")
std1 = np.load("std_final_1.npy")
w = Windowing(type = 'hann')
spectrum = Spectrum()
mfcc = GFCC(highFrequencyBound = 8000, numberCoefficients = 40, sampleRate = 16000)

current_milli_time = lambda: int(round(time.time() * 1000))

app = FlaskAPI(__name__)
CORS(app)
socketio = SocketIO(app, async_handlers = True)
rate = 16000
duration = 3



def normalize_data(data):
    return (data - mean1)/std



def to_feature(data):
    features = []
    st = 0
    et = st + 48000
    while et <= data.shape[0]:
        data1 = butter_highpass_filter(data[st: et], 100, 16000, 9).astype(np.float32)
        features.append(normalize_data(get_mfcc(data1)))
        st = et
        et = st + 48000
    return np.array(features)


def predict(features):
    res = model.predict(features)
    label = res > 0.5
    return label


class ClientData():
    def __init__(self, labels, start_times, end_times, samples, filename):
        self.labels = labels
        self.start_times = start_times
        self.end_times = end_times
        self.samples = samples
        self.filename = filename

    def save(self):
        wavfile.write(tomcat_path+"init.wav", 16000, (self.samples*32768).astype(np.int16))

    def to_dict(self):
        global tomcat_url
        return {
            "labels": self.labels,
            "start_times": self.start_times,
            "end_times": self.end_times,
            "url": tomcat_url+self.filename
        }


list_data = []
_, nw = wavfile.read("noise.wav")
def filter(w):
    global nw
    sr = 16000
    nsr = 16000
    s= librosa.stft(w)
    ss= np.abs(s)
    angle= np.angle(s)
    b=np.exp(1.0j* angle)
    ns= librosa.stft(nw) 
    nss= np.abs(ns)
    mns= np.mean(nss, axis=1) 
    sa= ss - mns.reshape((mns.shape[0],1)) 
    sa0= sa * b 
    y= librosa.istft(sa0)
    return y
class Data():
    def __init__(self, sample, label, time):
        self.sample = sample
        self.label = label
        self.time = time

    def to_dict(self):
        return {
            "sample": self.sample,
            "label": self.label,
            "time": self.time
        }


class DataFrame():
    def __init__(self, start_sample, end_sample, label):
        self.start_sample = start_sample
        self.end_sample = end_sample
        self.label = label

    def to_dict(self):
        global rate, duration
        starttime = self.start_sample/rate
        endtime = (self.end_sample-1)/rate
        return {
            'starttime': starttime,
            'endtime': endtime,
            'label': self.label
        }


def extract_mfcc(data):
    global rate
    return get_mfcc(data)


model = load_model('model_final_1.h5')

model._make_predict_function()

def get_mfcc(data):
    mfccs = []
    for frame in FrameGenerator(data, frameSize=1024, hopSize=512, startFromZero=True):
        mfcc_bands, mfcc_coeffs = mfcc(spectrum(w(frame)))
        mfccs.append(mfcc_coeffs)
    return np.asarray(mfccs)

def detect_wav(samples):
    global rate, duration, model
    samples_length = len(samples)
    duration_sample = duration*rate
    outputs = []
    
    t = 0
    step = int(duration_sample)
    while True:
        start_sample = t
        end_sample = start_sample+duration_sample

        if end_sample > samples_length:
            break

        outputs.append(DataFrame(start_sample, end_sample, 'None'))
        t += step

    hihi = []
    current = None
    i_t = 1
    for a in outputs:
        npa = np.array(samples[a.start_sample: a.end_sample])
        npa = npa.astype(np.float32)
        logbank = get_mfcc(npa)
        logbank = (logbank - mean1) / std1
        logbank = logbank.reshape(1, 93, 40)
        arr = model.predict(logbank)[0]
        result = 1
        if arr[0] < 0.5:
            result = 0

        if result == 0:
            a.label = "Wheeze"
        if result == 1:
            a.label = "Other Lung Sounds"
        if result == 2:
            a.label = "Wheeze And Crackle"

        i_t += 1
        if current is None:
            current = DataFrame(a.start_sample, a.end_sample, a.label)
        elif current.label == a.label:
            current.end_sample = a.end_sample
        else:
            hihi.append(current.to_dict())
            current = DataFrame(a.start_sample, a.end_sample, a.label)

    if not current is None:
        hihi.append(current.to_dict())

    return hihi




def detect(samples):
    global dem, rate, duration, model, number, preframe0, preframe1, preframe2, preframe3, preframe4
    

    
    test_data = butter_highpass_filter(samples, 100, 16000, 9).astype(np.float32)

    number += 1

    mfcc_feat = get_mfcc(test_data)

    mfcc_feat = (mfcc_feat - mean1) / std1
    mfcc_feat = mfcc_feat.reshape(1, 93, 40)

    tam = model.predict(mfcc_feat)
    arr = tam[0]

    result = 1
    if arr[0] < 0.5:
        result = 0

    if result == 0:
        k = "Wheeze"
    if result == 1:
        k = "Other"
    if result == 2:
        k = "WheezeAndCrackle"
    outfile = "/media/thanhptit/New Volume/Thesis/Application/python_web/web1/record_result/" + k + "_" + str(number)+".ogg"
    outfile1 = "/media/thanhptit/New Volume/Thesis/Application/python_web/web1/record_result/" + k + "_" + str(number)+".wav"
    linktofile.append(outfile)
    labeloffile.append(k)
    print(k)
    return k



@app.route("/", methods=[ 'GET'])
def home():
    if request.method == 'GET':
        return render_template('home.html')


@app.route("/api", methods=[ 'POST'])
def detect_api():
    if request.method == 'POST':
        global list_data
        ticstart = time.time()
        a = request.get_json()
        res = {
            "label": detect(a['data']),
            "time": a['time']
        }
        ticstop = time.time()
        print("xu ly mat: " + str(ticstop - ticstart))
        list_data.append(Data(a['data'][::16], res["label"], a['time']))

        return res, status.HTTP_200_OK

@app.route("/api_wav", methods=[ 'POST'])
def detect_wav_api():
    if request.method == 'POST':
        a = request.get_json()
        name = a["name"]
        name = name.split("/")[-1]
        rate, data = wavfile.read(name)
        res = detect_wav(data)
        return res, status.HTTP_200_OK


@app.route("/sample", methods=['GET'])
def get_sample():
    global list_data
    if len(list_data) != 0:
        return list_data.pop(0).to_dict(), status.HTTP_200_OK
    else: 
        return "nothing", status.HTTP_400_BAD_REQUEST
@app.route("/real_time", methods=['GET'])
def get_result():
    c = []
    c.append(linktofile)
    c.append(labeloffile)
    return jsonify(c)
@app.route("/reset", methods=['GET'])
def reset_result():
    global linktofile, labeloffile
    linktofile = []
    labeloffile = []
    return jsonify(linktofile)


@app.route("/create_file", methods=["POST"])
def create_file():
    global data_raw_labels, data_raw, list_data_client
    a = request.get_json()
    increase = float(a["increase"])
    noise = a["noise"]
    ffilter = int(a["filter"])
    print(increase)

    pure_dat = list_data_client[0]

    number = len(list_data_client)
    new_samples = pure_dat.samples*increase
    new_samples = np.minimum(new_samples, 1)
    new_samples = np.maximum(new_samples, -1)
    print(new_samples)
    print(predict(to_feature(new_samples)))

    new_labels = predict(to_feature(new_samples))

    cc_labels = []
    for label in new_labels:
        if label[0] == False:
            cc_labels.append("Wheeze")
        else:
            cc_labels.append("Normal")

    new_dat = ClientData(cc_labels, pure_dat.start_times, pure_dat.end_times, new_samples, str(number)+".wav")
    list_data_client.append(new_dat)
    wavfile.write(tomcat_path+str(number)+".wav", 16000, (new_samples*32768).astype(np.int16))
    print(new_dat.to_dict())
    return new_dat.to_dict()


@app.route("/init_file", methods=["POST"])
def init_file():
    global data_raw_labels, data_raw, list_data_client
    a = request.get_json()
    rand_seed = int(a["random_seed"])
    num = int(a["file_number"])
    np.random.seed(rand_seed)
    new_files, new_labels = shuffle(data_raw, data_raw_labels)
    c_rand = np.random.choice(len(new_files), num)
    c_files = []
    c_labels = []
    data_samples = np.array([])
    for i in c_rand:
        c_files.append(new_files[i])
        c_labels.append(new_labels[i])

    for filename in c_files:
        _, samples = wavfile.read(filename)
        data_samples = np.append(data_samples, samples)
    

    wavfile.write(tomcat_path+"init.wav", 16000, (data_samples*32768).astype(np.int16))

    start_times = []
    end_times = []
    for i in range(len(c_files)):
        start_times.append(3*i)
        end_times.append(3*i+2.9999)

    cc_labels = []
    for label in c_labels:
        if label == 0:
            cc_labels.append("Wheeze")
        else:
            cc_labels.append("Normal")

    res = {
        "labels": cc_labels,
        "start_times": start_times,
        "end_times": end_times,
        "url": tomcat_url+"init.wav"
    }

    list_data_client = [ClientData(cc_labels, start_times, end_times, data_samples, "init.wav")]
    return res
if __name__ == "__main__":
    #app.run(debug=True, host="0.0.0.0")
    socketio.run(app, port = 5000, host = "0.0.0.0")
