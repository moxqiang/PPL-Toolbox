package process.contact;

import process.contact.mygraph.Edge;
import process.contact.mygraph.IDirectGraph;
import process.contact.mygraph.ListDirectGraph;
import process.mapping.ReadBed;

import java.util.*;

public class GraphConcatemer {
//    Map<ReadAT, Long[]> monomers;
    List<ReadAT> monomers = new ArrayList<ReadAT>();
    Double penalty = 0.0;
    Double oPenalty = 0.0;
    Double gPenalty = 0.0;
    Double aPenalty = 0.0;

    public GraphConcatemer(List<ReadAT> monomers){
        this.monomers.addAll(monomers);
    }

    public void IdentifyMonomersFromConcatemer(double weightAS){
        IDirectGraph<ReadAT> graph = new ListDirectGraph<ReadAT>();
        List<ReadAT> newMonomers = new ArrayList<>();

        //图的建立：添加点
        ReadAT START = new ReadAT();
        START.rS=0;
        START.rE=0;
        ReadAT END = new ReadAT();
        END.rS=monomers.get(0).readLen;
        END.rE=monomers.get(0).readLen;
        monomers.add(START);
        monomers.add(END);
        for (ReadAT monomer :
                monomers) {
            graph.addVertex(monomer);
        }

        //图的建立：添加边
        for (ReadAT monomer1 :
                monomers) {
            for (ReadAT monomer2:
                 monomers) {
                if (
                        // 只有以下两种情况时n1和n2存在有向边
                        (monomer1.rE < monomer2.rE) ||
                        ((monomer1.rE == monomer2.rE) && (monomer1.rS < monomer2.rS))
                ){
                    int penaltyGO = monomer1.rE - monomer2.rS;
                    int penaltyG=0;
                    int penaltyO=0;
                    if (penaltyGO<0){
                        penaltyG = Math.abs(penaltyGO);
                    } else {
                        penaltyO = Math.abs(penaltyGO);
                    }
                    int penaltyA = monomer2.rE - monomer2.rS - monomer2.AS;
                    double weight= penaltyG + penaltyO + penaltyA;
                    double weightG= penaltyG;
                    double weightO= penaltyO;
                    double weightA= penaltyA;

//                    graph.addEdge(new Edge<>(monomer1,monomer2,weight));
                    graph.addEdge(new Edge<>(monomer1,monomer2, weight, weightO, weightG, weightA));
                }
            }
        }
        newMonomers=graph.BellmanFord(START, END);
        //计算penalty
        for (int i = 0; i < newMonomers.size()-1; i++) {
            Edge<ReadAT> edge = graph.getEdge(newMonomers.get(i), newMonomers.get(i + 1));
            this.penalty += edge.getWeight();
            this.oPenalty += edge.getWeightO();
            this.gPenalty += edge.getWeightG();
            this.aPenalty += edge.getWeightA();
        }
        //去除start和end节点
        newMonomers.remove(0);
        newMonomers.remove(newMonomers.size()-1);
        monomers = newMonomers;

    }


}
