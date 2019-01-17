package com.lnsf.example.botkuntweet

import com.atilika.kuromoji.ipadic.Tokenizer

class TweetGenerator(private var tweets: List<Tweet>) {
    private val begin = "__BEGIN__"
    private val end = "__END__"

    fun generate(): String {

        val tokenizeList = convertSentenceToBlock()     // 全てのツイートを単語ごとに分割してブロック化

        val availableBlockLists = generateSentencesFromBlocks(tokenizeList)   // ブロックを集めたリストを取得したツイートの数だけ作る

        val chosenBlockList = chooseSentenceFromList(availableBlockLists)   // 良さげなリストを選ぶ

        return this.formatSentence(chosenBlockList)     // ブロックのリストから文章を生成
    }

    private fun convertSentenceToBlock(): List<TokenizeBlock> {

        val blockList = mutableListOf<TokenizeBlock>()

        val tokenizer = Tokenizer.Builder().build()

        tweets.forEach { tweet ->
            val list = mutableListOf<TokenizeBlock>()
            val blocks = mutableListOf<Pair<String, Long>>()

            // 文頭の明示
            blocks.add(Pair(begin, tweet.id))

            tokenizer.tokenize(tweet.text).forEach { block ->
                blocks.add(Pair(block.surface, tweet.id))
            }

            // 文末の明示
            blocks.add(Pair(end, tweet.id))

            for (idx in 0 until blocks.size - 2) {
                list.add(
                    TokenizeBlock(
                        blocks[idx].first,
                        blocks[idx + 1].first,
                        blocks[idx + 2].first,
                        blocks[idx].second
                    )
                )
            }

            blockList.addAll(list)
        }

        return blockList.toList()
    }

    private fun generateSentencesFromBlocks(blocks: List<TokenizeBlock>): List<List<TokenizeBlock>> {
        val availableBlockLists = mutableListOf<List<TokenizeBlock>>()
        val heads = mutableListOf<TokenizeBlock>()

        // 文頭になり得るものを集める
        blocks.forEach { block ->
            if (block.first == begin) {
                heads.add(block)
            }
        }

        heads.forEach { head ->
            val blockList = mutableListOf<TokenizeBlock>()
            val blocksCopy = mutableListOf<TokenizeBlock>()
            blocksCopy.addAll(blocks)   // 深いコピーをしておく
            blockList.add(head)

            while (true) {
                val available = mutableListOf<TokenizeBlock>()
                blocksCopy.forEach { block ->
                    if (block.first == blockList[blockList.lastIndex].third) {
                        available.add(block)
                    }
                }

                if (available.size == 0) break

                val block = available.random()

                blockList.add(block)

                blocksCopy.remove(block)
            }

            availableBlockLists.add(blockList.toList())

        }

        return availableBlockLists.toList()
    }


    private fun chooseSentenceFromList(sts: List<List<TokenizeBlock>>): List<TokenizeBlock> {
        val tmp = mutableListOf<List<TokenizeBlock>>()  // 文章候補

        sts.forEach { st ->
            // もとのツイートとの類似度
            var cost = 0
            for (idx in 0 until st.size - 1) {
                if (st[idx].id == st[idx + 1].id) cost++    // 前後のブロックのツイートIDが同じならば類似度UP
            }

            if (st.size - cost > 2) {
                tmp.add(st.toList())    //　類似度があまりにも高すぎるものを除いて候補を追加
            }
        }

        return tmp.random()
    }

    private fun formatSentence(st: List<TokenizeBlock>): String {
        var fmtString = ""

        val fmtBlock: (TokenizeBlock) -> String = { b ->
            var s = ""
            s += if (b.second != begin) b.second else " "
            s += if (b.third != end) b.third else " "
            s
        }

        st.forEach { fmtString += fmtBlock(it) }

        return fmtString
    }

}