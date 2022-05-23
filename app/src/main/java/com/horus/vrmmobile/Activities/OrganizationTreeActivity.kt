package com.horus.vrmmobile.Activities

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.horus.vrmmobile.R
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.horus.vrmmobile.Models.PersonHierarchicalStructure
import com.horus.vrmmobile.Repositories.PersonHierarchicalStructureRepository
import com.horus.vrmmobile.Repositories.PositionRepository
import de.blox.graphview.*
import de.blox.graphview.tree.BuchheimWalkerAlgorithm
import de.blox.graphview.tree.BuchheimWalkerConfiguration
import kotlinx.android.synthetic.main.activity_organization_tree.*
import kotlinx.android.synthetic.main.item_participant_2.view.*

class OrganizationTreeActivity : AppCompatActivity() {

    private val graph = Graph()
    private val triples = ArrayList<Triple<String, String?, Node>>()
    private val positionMap = PositionRepository.instance.getAll().map { s -> s.Id to s.Name }.toMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_tree)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.purple)
        }

        val graphView: GraphView = findViewById(R.id.graph)

        triples.clear()
        triples.addAll(PersonHierarchicalStructureRepository.instance.getAll().map { s -> Triple(s.Id,s.ParentId,Node(s)) })
        txt_count.text = "${triples.size} miembros"
        val personHierarchicalIds = triples.map { s -> s.first }
        val roots = triples.filter { s -> s.second.isNullOrEmpty() || !personHierarchicalIds.contains(s.second!!) }
        roots.forEach { s -> setNodeRecursive(s) }

         val adapter = object: BaseGraphAdapter<SimpleViewHolder>(graph) {

            override fun  onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
                 val view = LayoutInflater.from(this@OrganizationTreeActivity).inflate(R.layout.item_participant_2, parent, false)
                return  SimpleViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: SimpleViewHolder?, data: Any?, position: Int) {
                val item = data as PersonHierarchicalStructure
                viewHolder!!.txtName.text = "${item.Person!!.FirstName} ${item.Person!!.FirstSurname}"
                viewHolder.txtPosition.text = positionMap[item.PositionId]!!
            }
        }
        graphView.adapter = adapter

         val configuration =  BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
                .build()
        adapter.algorithm = BuchheimWalkerAlgorithm(configuration)
    }

    internal inner class SimpleViewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.txt_name_person
        var txtPosition = itemView.txt_position
    }

    private fun setNodeRecursive(root: Triple<String, String?, Node>){
        val childs = triples.filter { s -> s.second == root.first }
        childs.forEach { s ->
            graph.addEdge(root.third, s.third)
            setNodeRecursive(s)
        }
    }
}

