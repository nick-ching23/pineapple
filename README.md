<h1 align="center">Veritas: A lightweight Generative AI detection service</h1>
<p align="center">
Collaborators: Nick Ching (nc2935), Naren Loganathan (nl2878), Suwei Ma (sm5011), Avery Fan (mf3332)
  
Contributions
[Trello link](https://trello.com/invite/b/6702d91258eab0e42ba8174c/ATTId2e43923e75399a4283c25456224c3a27CF0F058/pineapple-veritas)

</p>


## **About The Project**

Veritas is a lightweight and flexible service that abstracts the task of detecting AI-generated text in various contexts. Utilizing the cutting edge paper: [Raidar: geneRative AI Detection viA Rewriting](https://arxiv.org/pdf/2401.12970) published by Mao, Vondrick, Wang, and Yang. 



## **Current Progress**
  - **18 Oct 2024:** Completed development of Vertias service (including Java API controller, DB handlers and Python Microservice)


---

## **Vertias Architecture**

![Blank diagram (2)](https://github.com/user-attachments/assets/bc7b328e-5428-48c5-be8a-d493eaf83da6)


Veritas's Service utilizes a modular microservice design:
- Java Springboot API Handler: controls all business service logic, handling interactions with the ML model and our persistent storage
- Python ML microservice: this microservice is solely responsible for detecting AI-generated text. It is deployed on a separate GCP VM so and interacted with through HTTP
- Cloud SQL on GCP: The final component of our service is persistent storage, hosted on GCP. 



## **Project Requirements** 
1. Java Development Kit (JDK): Version 17 or later
   - Ensure that javac and java are installed and properly configured in your system's PATH.
   - you can verify this by running `javac --version` and `java-version`

2. Maven: Used for building the project
   - Maven should be installed and accessible via the CLI
   - you can verify this by running `mvn --version`
  
4. Python 3.11 or later: For deploying the Veritas microservice:
   - You can check your version using `python3 --version`
  
5. Intellij: Note that this project was built using Intellij IDEA, but it should work with any Java-compatible IDE. 


## **How to build our project** 
// what we need to run, compile and run our program locally 

## **Interacting with our service**
// what URL + port etc. we need to interact with the API 

## **Service Endpoint Descriptions**

<details>
<summary>GET: /</summary>
  <li>Purpose: Debugging function to ensure our API is connected. </li>
  <li>Expected Parameters: N/A</li>
  <li>Expected Output: "Welcome to Veritas!" string</li>
</details>

<details>
<summary>POST: /checkText</summary>
  <li>Purpose: Simply determine if an independent piece of text was potentially generated by AI</li>
  <li>Expected Parameters: String text</li>
  <li>Expected Output: HTTP OK Status with JSON containing a boolean true or false value</li>
  <li>Upon Failure: HTTP Bad Request or Internal Server Error</li>

</details>

<details>
<summary>POST: /checkTextUser</summary>
  <li>Purpose: Determine if a piece of text attributed to a user in an organization was
potentially generated by AI, updating the corresponding flag count in the database if so</li>
  <li>Expected Parameters: String text, String userId, String orgId</li>
  <li>Expected Output: HTTP OK, indicating that the text was analyzed (and that the database was
successfully updated if necessary)</li>
<li>Upon Failure: HTTP Bad Request or Internal Server Error (if supplied parameters are null
or the underlying checkText call errs out)</li>
</details>

<details>
<summary>GET: /numFlags</summary>
  <li>Purpose: Checks the number of times a particular user has been flagged for AI-generated text (from DB)</li>
  <li>Expected Parameters: String userId, String orgId</li>
  <li>Expected Output: HTTP OK, with the number of flags (int)</li>
</details>


## **Running Tests**
Run `mvn test` on the command line from the `veritas` directory. As of now, this runs 18 tests with high branch coverage. We have 3 suites of unit tests for the `VeritasController`, `VeritasRecord` and `VeritasService` classes.

## **Style Check Report** 

We used the tool "checkstyle" to check the style of our code and generate style checking reports. Run `mvn checkstyle:check` from the `veritas` directory. Here is the report
as of the day of 10/18/2024 (These can be found in the reports folder):

![Checkstyle](reports/checkstyle_10182024.png)

## **Branch Coverage Reporting**

We used JaCoCo to perform branch analysis in order to see the branch coverage of the relevant code within the code base. Run `mvn jacoco:report` from the `veritas` directory and open (in browser) the `index.html` that gets generated at `target/site/jacoco`. See below for a screenshot of the output - indicating high branch coverage.


![Screenshot of a code coverage report from the plugin](reports/jacoco.png)

## **Tools used** 


## **Third Party API Documentation**
- OpenAI API
- GCP

