# Hall Management System UI Style Guide

## Overview

This document outlines the UI styling standards and component usage for the Hall Management System application.

## Color Palette

- Primary: `#2196f3` (Blue)
- Primary Light: `#6ec6ff`
- Primary Dark: `#0069c0`
- Secondary: `#424242` (Dark Gray)
- Light Gray: `#f5f5f5`
- Medium Gray: `#9e9e9e`
- Dark Gray: `#212121`

## Typography

- Font Family: System default
- Base Font Size: 14px

## Components

### Buttons

We provide three button types:

1. Primary (default blue)
2. Secondary (dark gray)
3. Danger (red)

Usage:

```java
// Primary button
StyledButton primaryBtn = new StyledButton("Save");

// Secondary button
StyledButton secondaryBtn = new StyledButton("Cancel", ButtonType.SECONDARY);

// Danger button
StyledButton dangerBtn = new StyledButton("Delete", ButtonType.DANGER);
```

### Form Groups

Form groups provide consistent layout and spacing for form elements:

```java
TextField nameInput = new TextField();
FormGroup nameGroup = new FormGroup("Name", nameInput);

// Mark as required
nameGroup.setRequired(true);
```

## Layout Guidelines

### Spacing

- Form field spacing: 8px
- Section spacing: 16px
- Container padding: 16px

### Responsive Design

All components are designed to be responsive and will adapt to their container size.

## Accessibility

- All interactive elements have proper ARIA labels
- Required form fields are marked with an asterisk (\*)
- Focus states are clearly visible
- Color contrast meets WCAG standards

## Theme Support

The UI system supports both light and dark themes through CSS classes.
