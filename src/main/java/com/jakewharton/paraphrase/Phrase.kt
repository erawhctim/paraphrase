package com.jakewharton.paraphrase

import com.google.common.collect.ImmutableList
import java.util.regex.Pattern

data class Phrase(
    val name: String,
    val documentation: String?,
    val string: String,
    val tokens: List<String>
) {
    companion object {
        private val PHRASE = Pattern.compile("\\{([a-z_]+)\\}")
    
        fun isPhrase(string: String): Boolean {
            return PHRASE.matcher(string).find()
        }
        
        fun from(name: String, string: String): Phrase {
            return Phrase(
                name,
                null /* TODO */,
                string,
                tokensFrom(string)
            )
        }
        
        fun tokensFrom(string: String): List<String> {
            val tokens = mutableListOf<String>()
            val matcher = PHRASE.matcher(string)
            while (matcher.find()) {
                tokens.add(matcher.group(1))
            }
            tokens.sort()  // Used to binary search at runtime.
            return ImmutableList.copyOf(tokens)
        }
    }
}