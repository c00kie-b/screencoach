package com.android.achievix.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class ModelHandler {
    private Interpreter tflite;
    private ScalerParams scalerParams;
    private Map<String, Integer> categoryMapping;
    private Map<String, Integer> labelMapping;
    private StandardScaler scaler;

    public interface PredictionCallback {
        void onPredictionCompleted(String category);
    }

    public ModelHandler(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context, "newmodel.tflite"));
            scalerParams = loadJsonFile(context, "scaler_params.json", ScalerParams.class);
            categoryMapping = loadJsonFile(context, "category_mapping.json", new TypeToken<Map<String, Integer>>() {}.getType());
            labelMapping = loadJsonFile(context, "label_mapping.json", new TypeToken<Map<String, Integer>>() {}.getType());
            scaler = new StandardScaler();
        } catch (IOException e) {
            Log.e("ModelHandler", "Error loading model or parameters", e);
        }
    }

    private ByteBuffer loadModelFile(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        byte[] model = new byte[is.available()];
        is.read(model);
        ByteBuffer modelBuffer = ByteBuffer.allocateDirect(model.length);
        modelBuffer.put(model);
        return modelBuffer;
    }

    private <T> T loadJsonFile(Context context, String fileName, Class<T> type) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        T data = new Gson().fromJson(reader, type);
        reader.close();
        return data;
    }

    private <T> T loadJsonFile(Context context, String fileName, Type type) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        T data = new Gson().fromJson(reader, type);
        reader.close();
        return data;
    }

    public void preprocessAndPredict(Context context, PredictionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                File csvFile = new File(context.getExternalFilesDir(null), "installed_apps.csv");
                Log.d("ModelHandler", "CSV File Path: " + csvFile.getAbsolutePath());

                if (!csvFile.exists()) {
                    Log.e("ModelHandler", "CSV file does not exist");
                    return "z";
                }
                if (!csvFile.canRead()) {
                    Log.e("ModelHandler", "CSV file is not readable");
                    return "z";
                }
                try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                    String[] nextLine;
                    reader.readNext(); // Skip header

                    int totalMinutes = 0;
                    int totalNumberOfUses = 0;
                    int totalMorningUses = 0;
                    int totalAfternoonUses = 0;
                    int totalEveningUses = 0;
                    int totalNightUses = 0;
                    String category = "Other"; // Default category

                    while ((nextLine = reader.readNext()) != null) {
                        String totalTimeForeground = nextLine[5];
                        totalMinutes += convertTimeToMinutes(totalTimeForeground);
                        category = nextLine[4]; // Extract category from the CSV
                    }

                    return predict(totalMinutes, totalNumberOfUses, totalMorningUses, totalAfternoonUses, totalEveningUses, totalNightUses, category);
                } catch (IOException | CsvValidationException e) {
                    Log.e("ModelHandler", "Error reading CSV or predicting: " + e.getMessage());
                }
                return "z";
            }

            @Override
            protected void onPostExecute(String result) {
                if (callback != null) {
                    callback.onPredictionCompleted(result);
                }
            }
        }.execute();
    }

    public String predict(int totalMinutes, int totalNumberOfUses, int totalMorningUses, int totalAfternoonUses, int totalEveningUses, int totalNightUses, String category) {
        int categoryValue = categoryMapping.getOrDefault(category, 0); // Default to 0 if category is not found

        double[] inputData = new double[]{
                totalMinutes,
                categoryValue // Include the category as part of the input data
        };

        double[] mean = scalerParams.getMean().stream().mapToDouble(Double::doubleValue).toArray();
        double[] scale = scalerParams.getScale().stream().mapToDouble(Double::doubleValue).toArray();

        scaler.fit(new double[][]{mean, scale});  // Fit the scaler using mean and scale
        double[] scaledInput = scaler.transform(inputData);  // Transform the input data

        // Convert double array to float array and ensure the length matches the model's expected input size
        float[] floatScaledInput = new float[scaledInput.length];
        for (int i = 0; i < scaledInput.length; i++) {
            floatScaledInput[i] = (float) scaledInput[i];
        }

        // Log the input data
        Log.d("ModelHandler", "Input Data: " + Arrays.toString(floatScaledInput));

        // Ensure the input tensor has the correct shape and size
        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 7}, org.tensorflow.lite.DataType.FLOAT32);
        inputBuffer.loadArray(floatScaledInput);

        float[][] output = new float[1][labelMapping.size()];
        tflite.run(inputBuffer.getBuffer(), output);
        int predictedIndex = getMaxIndex(output[0]);

        return getCategoryFromPrediction(predictedIndex);
    }

    private int getMaxIndex(float[] array) {
        int index = 0;
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    private String getCategoryFromPrediction(int index) {
        return labelMapping.entrySet().stream()
                .filter(entry -> entry.getValue().equals(index))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("unknown");
    }

    public int convertTimeToMinutes(String timeData) {
        if (timeData == null || timeData.isEmpty()) return 0;
        String[] parts = timeData.split(" ");
        int minutes = 0;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("hr") || parts[i].contains("hour")) {
                minutes += Integer.parseInt(parts[i - 1]) * 60;
            } else if (parts[i].contains("min")) {
                minutes += Integer.parseInt(parts[i - 1]);
            }
        }
        return minutes;
    }
}
