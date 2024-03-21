<img src="https://avatars2.githubusercontent.com/u/2810941?v=3&s=96" alt="Google Cloud Platform logo" title="Google Cloud Platform" align="right" height="96" width="96"/>

# Gemini 1.0 Pro Vision in BigQuery: Remote Function Implementation
Here we will create a function in BigQuery based on the Java Cloud Function that implements Gemini 1.0 Pro Vision foundation model. First we’ll create and deploy the Java Cloud Function to compare images using the Gemini 1.0 Pro Vision model and then will create the remote function in BigQuery that invokes the deployed Cloud Function. Remember, the same procedure can be followed for any remote function execution in BigQuery.

## Create the Java Cloud Function
We will build a Gen 2 Cloud Function in Java for validating test images against a baseline image stored in a dataset containing test image screenshots in an external table in BigQuery using the Gemini Pro Vision model (Java SDK) and deploy it to a REST endpoint.

## Java Cloud Function
Open Cloud Shell Terminal and navigate to the root directory or your default workspace path.
Click the Cloud Code Sign In icon from the bottom left corner of the status bar and select the active Google Cloud Project that you want to create the Cloud Functions in.
Click the same icon again and this time select the option to create a new application.
In the Create New Application pop-up, select Cloud Functions application:

## Create New Application pop up in Cloud Shell Editor
5. Select Java: Hello World option from the next pop-up:


## Create New Application pop up page 2
6. Provide a name for the project in the project path. In this case, “Gemini-BQ-Function”.

7. You should see the project structure opened up in a new Cloud Shell Editor view:


## New Java Cloud Function application project structure
8. Now go ahead and add the necessary dependencies within the <dependencies>… </dependencies> tag in the pom.xml file or replace your pom.xml with the below:
https://github.com/AbiramiSukumaran/Gemini-BQ-Function/blob/main/pom.xml
     
9. Change the name of your class from “HelloWorld.java” to something more meaningful. Let’s say “GeminiBigQueryFunction.java”. You will have to rename the class accordingly.

10. Copy the code below and replace the placeholder code in the file GeminiBigQueryFunction.Java:
    https://github.com/AbiramiSukumaran/Gemini-BQ-Function/blob/main/src/main/java/cloudcode/helloworld/GeminiBigQueryFunction.java

11. Now go to Cloud Shell terminal and execute the below statement build and deploy the Cloud Function:

gcloud functions deploy gemini-bq-fn --runtime java17 --trigger-http --entry-point cloudcode.helloworld.GeminiBigQueryFunction --allow-unauthenticated
The result for this would be a REST URL in the format as below :

https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/gemini-bq-fn

12. Test this Cloud Function by running the following command from the terminal:

gcloud functions call gemini-bq-fn --region=us-central1 --gen2 --data '{"calls":[["https://storage.googleapis.com/img_public_test/image_validator/baseline/1.JPG", "https://storage.googleapis.com/img_public_test/image_validator/test/2.JPG", "PROMPT_ABOUT_THE_IMAGES_TO_GEMINI"]]}'
Response for a random sample prompt:
![image](https://github.com/AbiramiSukumaran/Gemini-BQ-Function/assets/13735898/c4a48305-fd9e-4751-81e8-fdcf37500ace)

JSON Response string from the Cloud Function

Now that the generic Cloud Function for Gemini Pro Vision model implementation is ready, you use this endpoint directly on BigQuery data from within a BigQuery remote function.

Read more about it in this blog: [https://medium.com/@abidsukumaran/in-place-llm-insights-bigquery-gemini-for-structured-unstructured-data-analytics-fdfac0421626](url)
