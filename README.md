# SmartHomeLUIS
Example App: Android / Kotlin Smart Home Demo for the Microsoft LUIS service (Language Understanding)

Demo client for a language understanding service.

Enter a smart home related text in natural language (e.g., "turn lights on"). The app sends the text to the Microsoft LUIS backend using a HTTP REST request. The JSON results from the service are parsed in the app. It shows the top intent and entity on the screen, together with the full JSON response.

![](https://raw.githubusercontent.com/andijakl/SmartHomeLUIS/master/screenshots/smarthomeclient-turnonstaircaselights-demo.gif)

Steps to get the app running:
* Create a LUIS service. Step-by-step instructions for a LUIS service are available in the blog article [Using Natural Language Understanding, Part 3: LUIS Language Understanding Service](https://www.andreasjakl.com/using-natural-language-understanding-part-3-luis-language-understanding-service/). The article focuses on a health-related scenario and not on a smart home, but the same steps apply when training a different dictionary.
* Update the endpoint URL and the app key in MainActivity.kt 
* [Complete background information and step-by-step instructions](https://www.slideshare.net/andreasjakl/android-development-with-kotlin-part-2-internet-services-and-json)

## Related Information

Released under MIT License. See the LICENSE file for details.

Developed by Andreas Jakl
* https://www.andreasjakl.com/
* https://twitter.com/andijakl