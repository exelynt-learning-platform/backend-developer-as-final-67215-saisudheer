# backend-developer-as-final-67215-saisudheer
Final Project Assignment - This repository contains the complete final project code and documentation.
# Resource Booking System

A secure RESTful Resource Booking System built using Spring Boot, Java 17, Spring Security, JWT, JPA/Hibernate, and MySQL.

The application allows authenticated users to view available resources and create/manage their reservations. Administrators have full access to manage resources and reservations.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [User Roles and Permissions](#user-roles-and-permissions)
- [Reservation Status](#reservation-status)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [How to Run](#how-to-run)
- [Authentication](#authentication)
- [Resource APIs](#resource-apis)
- [Reservation APIs](#reservation-apis)
- [Filtering](#filtering)
- [Pagination](#pagination)
- [Sorting](#sorting)
- [Validation](#validation)
- [Reservation Conflict Handling](#reservation-conflict-handling)
- [Error Handling](#error-handling)
- [Swagger / OpenAPI](#swagger--openapi)
- [Seed Data](#seed-data)
- [Testing](#testing)
- [Security](#security)
- [HTTP Status Codes](#http-status-codes)
- [Build Commands](#build-commands)
- [Assignment Requirements Coverage](#assignment-requirements-coverage)
- [Author](#author)

---

# Project Overview

The Resource Booking System is a backend REST API for managing bookable resources and reservations.

A resource can represent:

- A room
- A vehicle
- Equipment
- Any other bookable item

Users can:

- Authenticate using JWT
- View available resources
- Create reservations
- View their own reservations
- Filter reservations
- Use pagination and sorting

Administrators can:

- Manage resources
- Manage reservations
- View all reservations
- Update reservation status
- Perform full CRUD operations

The application uses Spring Security with JWT authentication and role-based authorization.

---

# Features

## Authentication

- JWT-based authentication
- Login endpoint
- BCrypt password hashing
- Stateless authentication
- JWT token validation
- Role information stored in JWT

## Authorization

Two roles are supported:

- `ADMIN`
- `USER`

Role-based access control is implemented using Spring Security.

## Resource Management

- Create resources
- View resources
- View resource by ID
- Update resources
- Delete resources
- Resource availability management

## Reservation Management

- Create reservations
- View reservations
- View reservation by ID
- Update reservations
- Delete reservations
- Update reservation status
- Reservation ownership validation

## Reservation Features

- PENDING status
- CONFIRMED status
- CANCELLED status
- Decimal reservation price
- Start/end time validation
- Booking conflict detection
- Status filtering
- Minimum price filtering
- Maximum price filtering
- Pagination
- Optional sorting

## Database

- MySQL
- Spring Data JPA
- Hibernate
- Entity relationships
- Persistent reservation and resource data

## API Documentation

- Swagger / OpenAPI documentation
- REST API testing support

## Error Handling

- Validation errors
- Authentication errors
- Authorization errors
- Resource not found errors
- Reservation not found errors
- Booking conflict errors
- Meaningful HTTP status codes

---

# Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot | Backend framework |
| Spring Web | REST API |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| BCrypt | Password hashing |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build and dependency management |
| Bean Validation | Request validation |
| Swagger / OpenAPI | API documentation |
| JUnit | Testing |
| Mockito | Unit testing |
| MockMvc | API/integration testing |
| Postman | API testing |

---

# Architecture

The application follows a layered architecture.

```text
Client
  |
  v
Controller Layer
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
MySQL Database
