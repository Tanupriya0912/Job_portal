import React from "react";
import styled from "styled-components";
import Navbar from "../components/shared/Navbar";

const AboutUs = () => {
  return (
    <>
      <Navbar />
      <Wrapper>
        <div className="container">
          <div className="content">
            <h1 className="title">About Us</h1>
            <div className="description">
                <p>
                     This job portal application was developed by{" "}
                     <span className="highlight">Tanupriya Korde</span> in collaboration with{" "}
                     <span className="highlight">[Name Here]</span> as part of the{" "}
                     <strong>CDAC Final Project</strong>. The system is designed using a{" "}
                     <strong>microservices architecture</strong>.
                </p>
            </div>
          </div>
        </div>
      </Wrapper>
    </>
  );
};

const Wrapper = styled.section`
  min-height: 100vh;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .container {
    max-width: 800px;
    margin: 0 auto;
    background: white;
    border-radius: 12px;
    padding: 3rem 2rem;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  }

  .content {
    text-align: center;
  }

  .title {
    font-size: 2.5rem;
    color: #333;
    margin-bottom: 2rem;
    font-weight: 700;
  }

  .description {
    font-size: 1.1rem;
    line-height: 1.8;
    color: #555;
  }

  .description p {
    margin: 0;
  }

  .highlight {
    color: #667eea;
    font-weight: 600;
  }

  @media (max-width: 768px) {
    padding: 2rem 1rem;

    .container {
      padding: 2rem 1.5rem;
    }

    .title {
      font-size: 2rem;
    }

    .description {
      font-size: 1rem;
    }
  }
`;

export default AboutUs;
