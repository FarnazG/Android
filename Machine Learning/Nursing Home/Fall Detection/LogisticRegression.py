# -*- coding: utf-8 -*-

import pandas as pd
from keras.layers import Dropout
from keras.optimizer_v1 import SGD
from sklearn.model_selection import train_test_split
from tensorflow import keras
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense
from sklearn import preprocessing


df = pd.read_csv("Fall_Detection_Dataset.csv")

# check data has been read in properly
df.head()

# create a dataframe with all training data except the target column
X = df.drop(columns=["Label"])

# check that the target variable has been removed
X.head()

# separate target values
y = df["Label"].values

# encode class values as integers
encoder = preprocessing.LabelEncoder()
encoder.fit(y)
encoded_y = encoder.transform(y)

# split dataset into train and test data
X_train, X_test, y_train, y_test = train_test_split(X, encoded_y, test_size=0.2, random_state=1, stratify=y)

"""Building and training the model"""

model = Sequential()
model.add(Dense(124, activation='relu', input_shape=(6,)))
model.add(Dropout(0.5))
model.add(Dense(124, activation='relu'))
model.add(Dropout(0.1))
model.add(Dense(1, activation='sigmoid'))
model.summary()

model.compile(loss='binary_crossentropy',
              optimizer=SGD(lr=0.1, momentum=0.003),
              metrics=['acc'])

history = model.fit(X_train, y_train,
                    batch_size=20,
                    epochs=100,
                    verbose=1,
                    validation_data=(X_test, y_test))
score = model.evaluate(X_test, y_test, verbose=0)

"""Testing the model"""

# show first 5 model predictions on the test data
print(model.predict(X_test)[0:5])

# check accuracy of our model on the test data
print(model.score(X_test, y_test))
model.save('LogisticRegression.h5')

# convert h5 to tflite
from tensorflow import lite

loaded_model = keras.models.load_model('LogisticRegression.h5')
converter = lite.TFLiteConverter.from_keras_model(loaded_model)
converter.optimizations = [lite.Optimize.DEFAULT]
tflite_model = converter.convert()
open("LogisticRegression.tflite", "wb").write(tflite_model) 

