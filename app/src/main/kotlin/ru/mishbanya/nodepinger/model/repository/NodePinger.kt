package ru.mishbanya.nodepinger.model.repository

interface NodePinger {
    fun pingNode(): Long
}