package app.christopher.newsapp.util

//Generic class used to wrap around network responses
//To differentiate between errors and successes.
//Handle loading states

//Sealed classes are sort of abstract classes in which we
// can determine what classes are allowed to inherit from this sealed classes
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) { //Only these classes defined below are allowed to inherit from 'Resource'
    class Success<T>(data: T) : Resource<T>(data) //Here, T is not nullable because we are sure we have data if the network response is successful.
    class Error<T>(message: String?, data: T? = null) : Resource<T>(data, message) //Message is not nullable because if we have an error, then we have an error message obviously.
    class Loading<T> : Resource<T>() //This will return when our request was fired off to the server. Then when the response comes, we will emit the above error or success states.

}