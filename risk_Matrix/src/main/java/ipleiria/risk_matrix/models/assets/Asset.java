package ipleiria.risk_matrix.models.assets;
import ipleiria.risk_matrix.models.riskAssessment.RiskAssessment;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL)
    private List<RiskAssessment> riskAssessments;

    // Constructors
    public Asset() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<RiskAssessment> getRiskAssessments() { return riskAssessments; }
    public void setRiskAssessments(List<RiskAssessment> riskAssessments) { this.riskAssessments = riskAssessments; }
}