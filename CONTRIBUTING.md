# 🤝 Contributing to MediTrack

First off, thank you for taking the time to contribute to **MediTrack**! 🎉

Whether you're fixing a bug, improving documentation, or adding a new feature, your contribution is greatly appreciated.

Please read the following guidelines before creating an issue or submitting a Pull Request.

---

# 🚀 Getting Started

## 1. Fork the Repository

Click the **Fork** button on GitHub to create your own copy of the repository.

---

## 2. Clone Your Fork

```bash
git clone https://github.com/<your-username>/MediTrack.git
cd MediTrack
```

---

## 3. Add the Original Repository

```bash
git remote add upstream https://github.com/22oo1cso56mansoorkhan-cell/MediTrack.git
```

Verify:

```bash
git remote -v
```

---

## 4. Sync with the Latest Changes

Always update your local repository before starting new work.

```bash
git checkout main
git pull upstream main
```

---

## 5. Create a New Branch

Never work directly on the `main` branch.

```bash
git checkout -b feature/your-feature-name
```

Example branch names:

```text
feature/pdf-report
feature/medicine-reminder
fix/database-crash
fix/ui-overflow
docs/readme-update
```

---

# 💻 Development Guidelines

Please follow these practices while contributing:

* Follow the existing project structure.
* Keep your code clean and readable.
* Use meaningful variable and method names.
* Avoid adding unnecessary dependencies.
* Test your changes before submitting.
* Keep each Pull Request focused on a single issue.
* Update documentation whenever necessary.

---

# 📝 Commit Message Guidelines

Use meaningful commit messages.

Examples:

```text
feat: add PDF report generation

fix: resolve medicine reminder notification issue

docs: update README with installation steps

refactor: simplify database helper methods
```

---

# 🐛 Issue Template

Before opening a new issue:

* Search existing issues to avoid duplicates.
* Clearly describe the problem or enhancement.
* Provide enough information for others to reproduce it.

Use the following template:

```md
## 📝 Issue Title

Short and descriptive title

---

## 📖 Description

Provide a detailed explanation of the issue or feature request.

### Current Behavior

Explain what currently happens.

### Expected Behavior

Explain what should happen.

### Steps to Reproduce (Bug Reports)

1.
2.
3.

### Screenshots (Optional)

Attach screenshots if applicable.

### Additional Context

- Android Version
- Device Name
- Logs (if available)
- Any additional information
```

---

# 🔀 Pull Request Template

Before opening a Pull Request:

* Make sure your branch is up to date.
* Test your changes.
* Resolve merge conflicts.
* Link the related issue whenever possible.

Use the following template:

```md
# 📋 Pull Request

## 📌 Description

Briefly explain your changes.

---

## 🔗 Related Issue

Fixes #

---

## 🛠 Type of Change

- [ ] Bug Fix
- [ ] New Feature
- [ ] Enhancement
- [ ] Documentation
- [ ] Refactoring
- [ ] Performance Improvement
- [ ] UI Improvement

---

## ✅ Checklist

- [ ] Code builds successfully
- [ ] Tested locally
- [ ] No compilation errors
- [ ] Documentation updated (if required)
- [ ] Screenshots added (if UI changes)

---

## 📸 Screenshots

(Optional)

---

## 💬 Additional Notes

Anything reviewers should know.
```

---

# 📋 Pull Request Checklist

Before submitting your Pull Request, ensure that:

* Your branch is created from the latest `main` branch.
* Your code follows the project's coding style.
* The application builds successfully.
* All features work as expected.
* No unnecessary files are included.
* Documentation has been updated if required.
* The Pull Request is linked to the corresponding issue.

---

# 💡 Feature Requests

When suggesting a feature, include:

* Problem statement
* Proposed solution
* Benefits
* Mockups or screenshots (optional)

---

# 🐞 Bug Reports

A good bug report should include:

* Android Version
* Device Model
* Steps to reproduce
* Expected behavior
* Actual behavior
* Screenshots (if applicable)

---

# ❤️ Code of Conduct

Please be respectful and professional while interacting with other contributors.

Constructive discussions, code reviews, and suggestions are always welcome.

Let's work together to make **MediTrack** a better project for everyone.

Happy Coding! 🚀
