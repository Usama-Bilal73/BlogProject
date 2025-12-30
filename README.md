BlogProject

An Android blogging application that allows users to create, save, and view articles. It integrates Firebase for authentication and Firestore database storage, and also includes AI-powered features using Groq AI.

Features

User Authentication: Sign up, login, and profile management.
Add Articles: Users can write articles with a title and description.
View Articles: Display all articles in a RecyclerView on the main screen.
Saved Blogs: Save favorite articles for offline viewing.
Blog Details: View article content in detail..
Like & Share: Users can like, save, and share articles.
Profile Management: Edit profile and view your own articles.

Tech Stack
Language: Kotlin, Java
Framework: Android SDK
Database: Firebase Firestore
Authentication: Firebase Auth
UI: XML layouts, RecyclerView
Project Structure
BlogProject
│
├─ app/src/main/java/com/example/blogproject/
│  ├─ AddArticalActivity.kt        # Add article functionality
│  ├─ BlogAdapter.kt               # RecyclerView adapter for blogs
│  ├─ BlogDetailsActivity.kt       # View article details
│  ├─ BlogItemModel.kt             # Data model for blogs
│  ├─ MainActivity.kt              # Main activity showing all blogs
│  ├─ SavedBlogsActivity.kt        # User's saved blogs
│  ├─ ProfileActivity.kt           # User profile
│  ├─ SinginRegistrationActivity2.kt # Registration & login
│  ├─ SplashActivity.kt            # App splash screen
│
├─ res/layout/                      # XML layouts for UI
├─ res/drawable/                    # Images, icons, and shapes
├─ res/values/                      # Colors, strings, and styles
└─ AndroidManifest.xml

Installation
Clone the repository:
git clone : https://github.com/Usama-Bilal73/BlogProject
Open in Android Studio.
Sync Gradle and build the project.
Add your Firebase configuration files (google-services.json) in app/.
Run the app on an emulator or device.

Usage

Register or log in with your account.
Add a blog article via the Add Article button.
View all articles on the main screen.
Save or like blogs as needed.
Access AI-powered summarization for blogs.

Contributing
Fork the repository.
Create a new branch: Spleshscreen

Commit your changes: git commit -m "Add feature"

Push to the branch: git push origin feature-name
