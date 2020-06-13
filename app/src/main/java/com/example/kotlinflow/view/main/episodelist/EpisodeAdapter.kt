package com.example.kotlinflow.view.main.episodelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinflow.app.load
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.databinding.ListItemEpisodeBinding

class EpisodeAdapter : ListAdapter<Episode, RecyclerView.ViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val episode = getItem(position)
        (holder as EpisodeViewHolder).bind(episode)
    }

    class EpisodeViewHolder(private val binding: ListItemEpisodeBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: Episode) {
            binding.apply {
                episodeName.text = episode.name
                episodeImage.load(episode.posterPath)
            }
        }
    }
}

private class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {

    override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem.episodeId == newItem.episodeId
    }

    override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return  oldItem == newItem
    }
}